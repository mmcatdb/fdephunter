export type ArmstrongRelation = {
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    referenceRow: ReferenceRow;
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

export type ReferenceRow = {
    values: string[];
};

export type ExampleRow = {
    values: string[];
    /** The indexes of the columns that form the maximal set. Undefined for the first row. */
    maxSet: number[];
    /** Whether it is a negative or positive example. Undefined for the first row. */
    isPositive: boolean;
    state: ExampleState;
    workerId?: string;
};

export enum ExampleState {
    New = 'NEW',
    Rejected = 'REJECTED',
    Accepted = 'ACCEPTED',
    Answered = 'ANSWERED',
    Conflict = 'CONFLICT',
}

export type ExampleRelation = {
    columns: string[];
    referenceRow: ReferenceRow;
    exampleRow: ExampleRow;
};

export type Lattice = {
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
    Final = 'final',
    Initial = 'initial',
    Subset = 'subset',
    Genuine = 'genuine',
    Candidate = 'candidate',
    Derived = 'derived',
    Eliminated = 'eliminated',
    Targeted = 'targeted',
    Coincidental = 'coincidental',
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

export const MOCK_ARMSTRONG_RELATIONS: ArmstrongRelation[] = [ {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maxSet: [ 1, 2 ], isPositive: true, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
        { maxSet: [ 1, 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
        { maxSet: [ 1, 4 ], isPositive: true, state: ExampleState.New, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
        { maxSet: [ 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0143942', 'S.O.S. Titanic', '1997', '85', 'History' ] },
    ],
    isEvaluatingPositives: false,

    minimalFDs: 12,
    otherFDs: 40,
    lhsSize: 0,
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maxSet: [ 0 ], isPositive: false, state: ExampleState.New, values: [ 'tt0036443', 'S.O.S. Titanic', '1979', '194', 'Drama+History' ] },
        { maxSet: [ 1, 2 ], isPositive: true, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '87', 'Drama+Romance' ] },
        { maxSet: [ 1, 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0115392', 'Titanic', '1996', '85', 'History' ] },
        { maxSet: [ 1, 4 ], isPositive: true, state: ExampleState.New, values: [ 'tt0120338', 'Titanic', '1997', 'null', 'Action+Drama+History' ] },
        { maxSet: [ 2 ], isPositive: false, state: ExampleState.New, values: [ 'tt0155274', 'The Titanic', '1943', '51', 'Documentary+Short' ] },
        { maxSet: [ 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0594950', 'Titanic Tech', '1915', '85', 'Documentary' ] },
        { maxSet: [ 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0771984', 'Titanic\'s Ghosts', '2006', '46', 'Action+Drama+History' ] },
    ],
    isEvaluatingPositives: false,

    minimalFDs: 9,
    otherFDs: 37,
    lhsSize: 1,
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maxSet: [ 1, 2 ], isPositive: true, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
        { maxSet: [ 1, 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
        { maxSet: [ 1, 4 ], isPositive: true, state: ExampleState.New, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
        { maxSet: [ 2, 3 ], isPositive: false, state: ExampleState.New, values: [ 'tt0155274', 'S.O.S. Titanic', '1943', '85', 'History' ] },
        { maxSet: [ 2, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0594950', 'Titanic Tech', '1943', 'null', 'Action+Drama+History' ] },
        { maxSet: [ 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0650185', 'The Titanic', '1997', '85', 'Action+Drama+History' ] },
    ],
    isEvaluatingPositives: false,

    minimalFDs: 12,
    otherFDs: 29,
    lhsSize: 2,
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maxSet: [ 1, 2, 3 ], isPositive: false, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '85', 'Drama+History' ] },
        { maxSet: [ 1, 2, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0115392', 'Titanic', '1943', '194', 'Action+Drama+History' ] },
        { maxSet: [ 1, 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0120338', 'Titanic', '1979', '85', 'Action+Drama+History' ] },
        { maxSet: [ 2, 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0155274', 'S.O.S. Titanic', '1943', '85', 'Action+Drama+History' ] },
    ],
    isEvaluatingPositives: false,

    minimalFDs: 4,
    otherFDs: 29,
    lhsSize: 3,
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maxSet: [ 1, 2, 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSet: [ 1, 2, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0115392', 'Titanic', '1943', '194', 'Action+Drama+History' ] },
        { maxSet: [ 1, 2 ], isPositive: true, state: ExampleState.New, values: [ 'tt0120338', 'Titanic', '1943', '87', 'Drama+History' ] },
        { maxSet: [ 1, 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0155274', 'Titanic', '1979', '85', 'Action+Drama+History' ] },
        { maxSet: [ 1, 3 ], isPositive: true, state: ExampleState.New, values: [ 'tt0594950', 'Titanic', '1996', '85', 'Drama+Romance' ] },
        { maxSet: [ 2, 3, 4 ], isPositive: false, state: ExampleState.New, values: [ 'tt0650185', 'S.O.S. Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSet: [ 2, 3 ], isPositive: false, state: ExampleState.New, values: [ 'tt0771984', 'The Titanic', '1943', '85', 'History' ] },
    ],
    isEvaluatingPositives: true,

    minimalFDs: 5,
    otherFDs: 28,
    lhsSize: 4,
} ];

export const MOCK_LATTICES: Lattice[][] = [
    [ {
        class: 'tconst',
        columns: [ 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [ {
            cells: [ McType.Subset, McType.Subset, McType.Subset, McType.Subset ],
        }, {
            cells: [ McType.Initial, McType.Initial, McType.Initial, McType.Candidate, McType.Candidate, McType.Candidate ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Derived ],
        }, {
            cells: [ McType.Derived ],
        } ],
    } ],
    [ {
        class: 'tconst',
        columns: [ 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [ {
            cells: [ McType.Subset, McType.Subset, McType.Subset, McType.Subset ],
        }, {
            cells: [ McType.Subset, McType.Subset, McType.Subset, McType.Coincidental, McType.Coincidental, McType.Coincidental ],
        }, {
            cells: [ McType.Coincidental, McType.Coincidental, McType.Coincidental, McType.Coincidental ],
        }, {
            cells: [ McType.Eliminated ],
        } ],
    }, {
        class: 'primaryTitle',
        columns: [ 'tconst', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [ {
            cells: [ McType.Genuine, McType.Coincidental, McType.Subset, McType.Coincidental ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Coincidental, McType.Coincidental, McType.Coincidental ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Eliminated ],
        }, {
            cells: [ McType.Derived ],
        } ],
    }, {
        class: 'startYear',
        columns: [ 'tconst', 'primaryTitle', 'runtimeMinutes', 'genres' ],
        rows: [ {
            cells: [ McType.Genuine, McType.Subset, McType.Subset, McType.Subset ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Subset, McType.Subset, McType.Coincidental ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Eliminated ],
        }, {
            cells: [ McType.Derived ],
        } ],
    }, {
        class: 'runtimeMinutes',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'genres' ],
        rows: [ {
            cells: [ McType.Genuine, McType.Subset, McType.Subset, McType.Subset ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Subset, McType.Subset, McType.Coincidental ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Eliminated ],
        }, {
            cells: [ McType.Derived ],
        } ],
    }, {
        class: 'genres',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes' ],
        rows: [ {
            cells: [ McType.Genuine, McType.Subset, McType.Subset, McType.Subset ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Final, McType.Final, McType.Eliminated ],
        }, {
            cells: [ McType.Derived, McType.Derived, McType.Derived, McType.Genuine ],
        }, {
            cells: [ McType.Derived ],
        } ],
    } ],
];
