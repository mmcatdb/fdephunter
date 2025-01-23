import { ExampleState } from './negativeExample';

export type ArmstrongRelation = {
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    referenceRow: ReferenceRow;
    exampleRows: ExampleRow[];
    /**
     * Whether the positive examples can be evaluated (otherwise only the negative ones).
     * Should be true if and only if all negative examples are evaluated.
     */
    isPositivesAllowed: boolean;
};

export type ReferenceRow = {
    values: string[];
};

export type ExampleRow = {
    values: string[];
    /** The indexes of the columns that form the maximal set. Undefined for the first row. */
    maximalSet: number[];
    /** Whether it is a negative or positive example. Undefined for the first row. */
    isNegative: boolean;
    state: ExampleState;
    workerId?: string;
};

export const MOCK_ARMSTRONG_RELATION: ArmstrongRelation = {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres'  ],
    referenceRow: { values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
    exampleRows: [
        { maximalSet: [ 1,2 ], isNegative: false, state: ExampleState.New, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
        { maximalSet: [ 1,3 ], isNegative: false, state: ExampleState.Accepted, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
        { maximalSet: [ 1,4 ], isNegative: false, state: ExampleState.Rejected, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
        { maximalSet: [ 3 ], isNegative: true, state: ExampleState.New, values: [ 'tt0143942', 'S.O.S. Titanic', '1997', '85', 'History' ] },
    ],
    isPositivesAllowed: false,
};

export type ExampleRelation = {
    columns: string[];
    referenceRow: ReferenceRow;
    exampleRow: ExampleRow;
};
