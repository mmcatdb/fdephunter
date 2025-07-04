import { type Lattice, CellType } from '@/types/armstrongRelation';

// export const MOCK_ARMSTRONG_RELATIONS: ArmstrongRelation[] = [ {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
//     exampleRows: [
//         { maxSetElement: { columns: [ 1, 2 ] }, isPositive: true, decision: undefined, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
//         { maxSetElement: { columns: [ 1, 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
//         { maxSetElement: { columns: [ 1, 4 ] }, isPositive: true, decision: undefined, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0143942', 'S.O.S. Titanic', '1997', '85', 'History' ] },
//     ],
//     isEvaluatingPositives: false,

//     minimalFds: 12,
//     otherFds: 40,
//     lhsSize: 0,
// }, {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
//     exampleRows: [
//         { maxSetElement: { columns: [ 0 ] }, isPositive: false, decision: undefined, values: [ 'tt0036443', 'S.O.S. Titanic', '1979', '194', 'Drama+History' ] },
//         { maxSetElement: { columns: [ 1, 2 ] }, isPositive: true, decision: undefined, values: [ 'tt0079836', 'Titanic', '1943', '87', 'Drama+Romance' ] },
//         { maxSetElement: { columns: [ 1, 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0115392', 'Titanic', '1996', '85', 'History' ] },
//         { maxSetElement: { columns: [ 1, 4 ] }, isPositive: true, decision: undefined, values: [ 'tt0120338', 'Titanic', '1997', 'null', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 2 ] }, isPositive: false, decision: undefined, values: [ 'tt0155274', 'The Titanic', '1943', '51', 'Documentary+Short' ] },
//         { maxSetElement: { columns: [ 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0594950', 'Titanic Tech', '1915', '85', 'Documentary' ] },
//         { maxSetElement: { columns: [ 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0771984', 'Titanic\'s Ghosts', '2006', '46', 'Action+Drama+History' ] },
//     ],
//     isEvaluatingPositives: false,

//     minimalFds: 9,
//     otherFds: 37,
//     lhsSize: 1,
// }, {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
//     exampleRows: [
//         { maxSetElement: { columns: [ 1, 2 ] }, isPositive: true, decision: undefined, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
//         { maxSetElement: { columns: [ 1, 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
//         { maxSetElement: { columns: [ 1, 4 ] }, isPositive: true, decision: undefined, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 2, 3 ] }, isPositive: false, decision: undefined, values: [ 'tt0155274', 'S.O.S. Titanic', '1943', '85', 'History' ] },
//         { maxSetElement: { columns: [ 2, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0594950', 'Titanic Tech', '1943', 'null', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 3, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0650185', 'The Titanic', '1997', '85', 'Action+Drama+History' ] },
//     ],
//     isEvaluatingPositives: false,

//     minimalFds: 12,
//     otherFds: 29,
//     lhsSize: 2,
// }, {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
//     exampleRows: [
//         { maxSetElement: { columns: [ 1, 2, 3 ] }, isPositive: false, decision: undefined, values: [ 'tt0079836', 'Titanic', '1943', '85', 'Drama+History' ] },
//         { maxSetElement: { columns: [ 1, 2, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0115392', 'Titanic', '1943', '194', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 1, 3, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0120338', 'Titanic', '1979', '85', 'Action+Drama+History' ] },
//         { maxSetElement: { columns: [ 2, 3, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0155274', 'S.O.S. Titanic', '1943', '85', 'Action+Drama+History' ] },
//     ],
//     isEvaluatingPositives: false,

//     minimalFds: 4,
//     otherFds: 29,
//     lhsSize: 3,
/*
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
    exampleRows: [
        { maxSetElement: { columns: [ 1, 2, 3, 4 ] }, isPositive: false, decision: undefined, values: [ 'tt0079836', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 2, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0115392', 'Titanic', '1943', '194', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 2 ] }, isPositive: true, decision: undefined, values: [ 'tt0120338', 'Titanic', '1943', '87', 'Drama+History' ] },
        { maxSetElement: { columns: [ 1, 3, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0155274', 'Titanic', '1979', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0594950', 'Titanic', '1996', '85', 'Drama+Romance' ] },
        { maxSetElement: { columns: [ 2, 3, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0650185', 'S.O.S. Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 2, 3 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0771984', 'The Titanic', '1943', '85', 'History' ] },
    ],
    isEvaluatingPositives: false,

    minimalFds: 4,
    otherFds: 28,
    lhsSize: 4,
}, {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    referenceRow: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
    exampleRows: [
        { maxSetElement: { columns: [ 1, 2, 3, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0079836', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 2, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0115392', 'Titanic', '1943', '194', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 2 ] }, isPositive: true, decision: undefined, values: [ 'tt0120338', 'Titanic', '1943', '87', 'Drama+History' ] },
        { maxSetElement: { columns: [ 1, 3, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0155274', 'Titanic', '1979', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 1, 3 ] }, isPositive: true, decision: undefined, values: [ 'tt0594950', 'Titanic', '1996', '85', 'Drama+Romance' ] },
        { maxSetElement: { columns: [ 2, 3, 4 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0650185', 'S.O.S. Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maxSetElement: { columns: [ 2, 3 ] }, isPositive: false, state: ExampleState.Accepted, values: [ 'tt0771984', 'The Titanic', '1943', '85', 'History' ] },
    ],
    isEvaluatingPositives: true,

    minimalFds: 4,
    otherFds: 28,
    lhsSize: 2,
*/
// } ];

export const MOCK_LATTICES: Lattice[][] = [
    [ {
        classColumn: 'tconst',
        columns: [ 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Subset, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Initial, CellType.Initial, CellType.Initial, CellType.Candidate, CellType.Candidate, CellType.Candidate ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'primaryTitle',
        columns: [ 'tconst', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Candidate, CellType.Candidate, CellType.Initial, CellType.Candidate ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'startYear',
        columns: [ 'tconst', 'primaryTitle', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Candidate, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Initial, CellType.Initial, CellType.Candidate ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'runtimeMinutes',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'genres' ],
        rows: [
            [ CellType.Candidate, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Initial, CellType.Initial, CellType.Candidate ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'genres',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes' ],
        rows: [
            [ CellType.Candidate, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Initial, CellType.Initial, CellType.Candidate ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Derived ],
            [ CellType.Derived ],
        ],
    } ],
    [ {
        classColumn: 'tconst',
        columns: [ 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Subset, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Subset, CellType.Subset, CellType.Subset, CellType.Coincidental, CellType.Coincidental, CellType.Coincidental ],
            [ CellType.Coincidental, CellType.Coincidental, CellType.Coincidental, CellType.Coincidental ],
            [ CellType.Eliminated ],
        ],
    }, {
        classColumn: 'primaryTitle',
        columns: [ 'tconst', 'startYear', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Genuine, CellType.Coincidental, CellType.Subset, CellType.Coincidental ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Coincidental, CellType.Coincidental, CellType.Coincidental ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Eliminated ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'startYear',
        columns: [ 'tconst', 'primaryTitle', 'runtimeMinutes', 'genres' ],
        rows: [
            [ CellType.Genuine, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Subset, CellType.Subset, CellType.Coincidental ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Eliminated ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'runtimeMinutes',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'genres' ],
        rows: [
            [ CellType.Genuine, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Subset, CellType.Subset, CellType.Coincidental ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Eliminated ],
            [ CellType.Derived ],
        ],
    }, {
        classColumn: 'genres',
        columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes' ],
        rows: [
            [ CellType.Genuine, CellType.Subset, CellType.Subset, CellType.Subset ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Final, CellType.Final, CellType.Eliminated ],
            [ CellType.Derived, CellType.Derived, CellType.Derived, CellType.Genuine ],
            [ CellType.Derived ],
        ],
    } ],
];

// export const MOCK_DATASET_DATA: DatasetData = {
//     header: [ 'tconst','primaryTitle','startYear','runtimeMin.','genres' ],
//     rows: [
//         [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMin.', 'genres' ],
//         [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
//         [ 'tt0079836', 'S.O.S. Titanic', '1979', '194', 'Drama+History' ],
//         [ 'tt0115392', 'Titanic', '1996', '87', 'Action+Drama+History' ],
//         [ 'tt0120338', 'Titanic', '1997', '194', 'Drama+Romance' ],
//         [ 'tt0155274', 'Titanic', '1915', '', 'History' ],
//         [ 'tt0594950', 'Titanic', '1997', '', 'Documentary+Short' ],
//         [ 'tt0771984', 'Titanic', '2006', '51', 'Documentary' ],
//         [ 'tt0902058', 'The Titanic', '1981', '', 'Documentary+Drama+Fantasy' ],
//         [ 'tt0650185', 'Titanic Tech', '2003', '46', 'Documentary+History' ],
//         [ 'tt0929378', 'Titanic\'s Ghosts', '2002', '57', 'Documentary+History+War' ],
//     ],
// };

// export const MOCK_DATASETS: DatasetResponse[] = [
//     'iris',
//     'balance-scale',
//     'chess',
//     'abalone',
//     'nursery',
//     'breast-cancer-wisconsin',
//     'bridges',
//     'echocardiogram',
//     'adult',
//     'letter',
//     'ncvoter',
//     'ncvoter',
//     'hepatitis',
//     'horse',
//     'fd-reduced-30',
//     'plista',
//     'flight',
//     'flight',
//     'uniprot',
//     'TPC H lineitem',
//     'School results',
//     'Adult',
//     'Classification',
//     'Reflns',
//     'Atom sites',
//     'DB status',
//     'Entity source',
//     'Bio entry',
//     'Voter',
//     'FDR-15',
//     'FDR-30',
//     'Atom',
//     'Census',
//     'Wiki image',
//     'Spots',
//     'Struct sheet',
//     'Ditag feature',
// ].map(name => ({
//     id: v4(),
//     name,
//     type: DatasetType.Csv,
//     source: 'TODO source',
// }));

// export const MOCK_FD_SETS: FdSet[] = [ {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     fdClasses: [ [
//         ColumnSet.fromIndexes([ 2, 3 ]),
//         ColumnSet.fromIndexes([ 2, 4 ]),
//         ColumnSet.fromIndexes([ 3, 4 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//         ColumnSet.fromIndexes([ 2 ]),
//         ColumnSet.fromIndexes([ 4 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//         ColumnSet.fromIndexes([ 3, 4 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//         ColumnSet.fromIndexes([ 2, 4 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//         ColumnSet.fromIndexes([ 2, 3 ]),
//     ] ],
// }, {
//     columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
//     fdClasses: [ [
//         ColumnSet.fromIndexes([ 0 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//     ], [
//         ColumnSet.fromIndexes([ 0 ]),
//     ], [
//         ColumnSet.fromIndexes([ 1, 2, 3 ]),
//     ],
//     ],
// } ];
