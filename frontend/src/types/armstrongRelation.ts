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

    /** This probably shouldn't be here, but we need it now for the stats. */
    minimalFDs: number;
    otherFDs: number;
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
    /** The indexes of the columns that form the maximal set. */
    maxSet: number[];
    /** Whether it is a negative or positive example. */
    isPositive: boolean;
    state: ExampleState;
};

export enum ExampleState {
    New = 'NEW',
    Rejected = 'REJECTED',
    Accepted = 'ACCEPTED',
    Undecided = 'UNDECIDED',
}

export type LatticeForClass = {
    class: string;
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    rows: LatticeRow[];
};

type LatticeRow = {
    /** States of the cells in the row. Each cell max set should be computable form its cell and row index. */
    cells: McType[];
};

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

/** Computes indexes of columns that form this max set. */
export function computeColumnIndexesForLatticeRow(rowIndex: number, columnsCount: number): number[][] {
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
    const maxIndexes = indexes.map(i => columnsCount + i - cellSize);

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

export function computeEdgesForLatticeCell(cell: number[], columnsCount: number): number[][] {
    const output = [];

    // Let's try to insert i between the values of the cell (for all possible i).
    // We know that our i is larger than cell values on all previous indexes.
    let maxUncheckedIndex = 0;

    for (let insert = 0; insert < columnsCount; insert++) {
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
