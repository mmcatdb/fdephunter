import { ColumnSet, type ColumnSetResponse } from './ColumnSet';

export type ExampleRowResponse = {
    values: string[];
    lhsSet: ColumnSetResponse;
    rhsSet: ColumnSetResponse;
    isPositive: boolean;
    decision: ExampleDecision | null;
};

export class ExampleRow {
    private constructor(
        readonly values: string[],
        /**
         * The indexes of the columns that form the lhs of FDs that are violated by the example.
         * It's a candidate element of all max sets for the columns in rhsSet).
         * It's an element (or a subset of one) of all max sets for the other columns (not in lhs nor rhs).
         */
        readonly lhsSet: ColumnSet,
        /**
         * The indexes of the columns that form the rhs of FDs that are violated by the example.
         * It's disjoint with lhsSet.
         */
        readonly rhsSet: ColumnSet,
        /** Whether it is a negative or positive example. */
        readonly isPositive: boolean,
        /** If undefined, the row is still undecided. */
        readonly decision: ExampleDecision | undefined,
    ) {}

    static fromResponse(input: ExampleRowResponse): ExampleRow {
        return new ExampleRow(
            input.values,
            ColumnSet.fromResponse(input.lhsSet),
            ColumnSet.fromResponse(input.rhsSet),
            input.isPositive,
            input.decision ?? undefined,
        );
    }
}

export type ExampleDecision = {
    status: DecisionStatus;
    /** The columns are in the same order as in the original relation. */
    columns: {
        /** If null, the column isn't a part of the example row's rhsSet (so it should be ignored). */
        status: DecisionColumnStatus | undefined;
        /** User provided strings. Probably not important right now. */
        reasons: string[];
    }[];
};

export enum DecisionStatus {
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    Unanswered = 'UNANSWERED',
}

export type DecisionColumn = {
    /** If undefined, the column isn't a part of the max set (so it should be ignored). */
    status: DecisionColumnStatus | undefined;
    /** User provided strings. Probably not important right now. */
    reasons: string[];
};

export enum DecisionColumnStatus {
    Undecided = 'UNDECIDED',
    Valid = 'VALID',
    Invalid = 'INVALID',
}

export type LatticesResponse = {
    lattices: Lattice[];
};

/**
 * The lattice of of elements for a specific class. Each element is a set of columns, the class is also a column. Each element then corresponds to an element of the max set for the class (max set of attributes of on the LHS that do not form a functional dependency with the class on the RHS).
 */
export type Lattice = {
    /** Name of the class column. */
    classColumn: string;
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    /** States of the cells in the rows. Each cell max set should be computable form its cell and row index. */
    rows: CellType[][];
};

/** A type of an element from the max set for a class. */
export enum CellType {
    GenuineFinal = 0,
    GenuineTemp = 1,
    GenuineDerived = 2,
    InvalidFinal = 3,
    InvalidTemp = 4,
    InvalidDerived = 5,
    FakeFinal = 6,
    FakeTemp = 7,
    FakeDerived = 8,
}

/**
 * For a given row index, returns an array of sets. Each set is an array of indexes of columns that form the max set for the given cell.
 */
export function computeColumnIndexesForLatticeRow(rowIndex: number, columnCount: number): number[][] {
    // The most bottom row (0) is: 0, 1, 2, ...
    // The next row (1) is: 01, 02, 03, ..., 12, 13, ...
    // The last row is: 0123...
    // However, there is always one column missing - i.e., if we want the lattice for class "0", then the first row should be 1, 2, 3, ... instead.
    // To solve this, we simply create a lattice for one less column and then map the columns accordingly. But that's out of the scope of this function.

    const output: number[][] = [];

    // The total number of columns in each cell.
    const cellSize = rowIndex + 1;
    // Starts at [ 0, 1, 2 ] (for the 3rd row).
    const indexes = [ ...new Array(cellSize).keys() ];
    // The max values of the indexes. Should be [ 7, 8, 9 ] (for 10 columns).
    const maxIndexes = indexes.map(i => columnCount + i - cellSize);

    while (indexes[0] <= maxIndexes[0]) {
        output.push([ ...indexes ]);

        let position = cellSize - 1;
        indexes[position]++;

        if (indexes[position] <= maxIndexes[position])
            continue;

        while (position > 0 && indexes[position] >= maxIndexes[position])
            position--;

        indexes[position]++;

        while (position < cellSize - 1) {
            position++;
            indexes[position] = indexes[position - 1] + 1;
        }
    }

    return output;
}

export function computeEdgesForLatticeCell(cell: number[], columnCount: number): number[][] {
    const output = [];

    // Let's try to insert i between the values of the cell (for all possible i).
    // We know that our i is larger than cell values on all previous indexes.
    let maxUncheckedIndex = 0;

    for (let insert = 0; insert < columnCount; insert++) {
        let skip = false;

        // First, we find the current largest index.
        for (; maxUncheckedIndex < cell.length; maxUncheckedIndex++) {
            if (cell[maxUncheckedIndex] > insert)
                break;

            if (cell[maxUncheckedIndex] === insert) {
                skip = true;
                maxUncheckedIndex++;
                break;
            }
        }

        // The number we want to insert is already there.
        if (skip)
            continue;

        // Now we can insert the number.
        const edge: number[] = [];

        for (let i = 0; i < maxUncheckedIndex; i++)
            edge.push(cell[i]);

        edge.push(insert);

        for (let i = maxUncheckedIndex; i < cell.length; i++)
            edge.push(cell[i]);

        output.push(edge);
    }

    return output;
}

export class CombinationIndexer {
    private readonly nChooseKCache: number[][] = [];
    private readonly maxIndexCache: number[] = [];

    constructor(
        private readonly n: number,
    ) {
        const maxK = n;

        // The largest N is going to be n - 0 - 1, i.e., n - 1. The smallest N will be n - (n - 1) - 1, i.e., 0.
        for (let N = 0; N < n; N++) {
            this.nChooseKCache[N] = [];

            // The largest K is going to be k - 0, i.e., k. The smallest K will be k - (k - 1), i.e., 1. But we can also compute (N 0), just for simplicity.
            for (let K = 0; K <= maxK; K++)
                this.nChooseKCache[N][K] = nChooseK(N, K);
        }

        for (let K = 1; K <= maxK; K++)
            this.maxIndexCache[K] = nChooseK(n, K) - 1;
    }

    public getIndex(c: number[]): number {
        let sum = 0;
        const n = this.n;
        const k = c.length;
        const cache = this.nChooseKCache;

        for (let i = 0; i < k; i++)
            sum += cache[n - c[i] - 1][k - i];

        return this.maxIndexCache[k] - sum;
    }
}

export function nChooseK(n: number, k: number): number {
    if (k > n)
        return 0;

    return factorial(n) / (factorial(k) * factorial(n - k));
}

const factorialCache = new Map<number, number>();

export function factorial(x: number): number {
    if (x <= 1)
        return 1;

    const cached = factorialCache.get(x);
    if (cached !== undefined)
        return cached;

    const result = x * factorial(x - 1);
    factorialCache.set(x, result);

    return result;
}
