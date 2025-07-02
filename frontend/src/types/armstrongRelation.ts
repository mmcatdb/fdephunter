import { type ColumnSet } from './ColumnSet';

export type ArmstrongRelation = {
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    /** Values of the reference row. */
    referenceRow: string[];
    exampleRows: ExampleRow[];
    /**
     * Whether we are evaluating the positive examples or the negative ones.
     * Should be true if and only if all negative examples are already evaluated.
     */
    isEvaluatingPositives: boolean;

    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    minimalFds: number;
    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    otherFds: number;
    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    lhsSize: number;
};

export type ExampleRelation = {
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    /** Values of the reference row. */
    referenceRow: string[];
    exampleRow: ExampleRow;
};

export type ExampleRow = {
    values: string[];
    /** The indexes of the columns that form the max set element. */
    maxSetElement: ColumnSet;
    /** Whether it is a negative or positive example. */
    isPositive: boolean;

    /** If undefined, the row is still undecided. */
    decision: ExampleDecision | undefined;
};

export type ExampleDecision = {
    status: DecisionStatus;
    columns: {
        /** If undefined, the column isn't a part of the max set (so it should be ignored). */
        status: DecisionColumnStatus | undefined;
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

/**
 * The lattice of of elements for a specific class. Each element is a set of columns, the class is also a column. Each element then corresponds to an element of the max set for the class (max set of attributes of on the LHS that do not form a functional dependency with the class on the RHS).
 */
export type Lattice = {
    /** Name of the class column. */
    classColumn: string;
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    /** States of the cells in the rows. Each cell max set should be computable form its cell and row index. */
    rows: McType[][];
};

/** A type of an element from the max set for a class. */
export enum McType {
    Final = 'FINAL',
    Initial = 'INITIAL',
    Subset = 'SUBSET',
    Genuine = 'GENUINE',
    Candidate = 'CANDIDATE',
    Derived = 'DERIVED',
    Eliminated = 'ELIMINATED',
    Targeted = 'TARGETED',
    Coincidental = 'COINCIDENTAL',
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
