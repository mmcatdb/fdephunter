export enum DatasetType {
    Csv = 'CSV',
    Json = 'JSON',
    Xml = 'XML',
    LabeledGraph = 'LABELED_GRAPH',
    Relational = 'RELATIONAL',
    Array = 'ARRAY',
    Rdf = 'RDF',
}

export type DatasetFromServer = {
    name: string;
    type: DatasetType;
    // TODO not important now
    // source: string;
    // kindName: string;
    // columns: number;
    // rows: number;
    // size: number;
    // fds: number;
};

export class Dataset {
    private constructor(
        readonly name: string,
        // TODO not important now
        // readonly source: string,
        // readonly columns: number,
        // readonly rows: number,
        // readonly size: string,
        // readonly FDs: number,
        // readonly orderDependencies: number,
    ) {}

    static fromServer(input: DatasetFromServer): Dataset {
        return new Dataset(input.name);
    }
}

export type DatasetHeader = string[];

export type DatasetRow = string[];

export type DatasetData = {
    header: DatasetHeader;
    rows: DatasetRow[];
};

export const MOCK_DATASET: DatasetData = {
    header: [ 'tconst','primaryTitle','startYear','runtimeMin.','genres' ],
    rows: [
        [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMin.', 'genres' ],
        [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ],
        [ 'tt0079836', 'S.O.S. Titanic', '1979', '194', 'Drama+History' ],
        [ 'tt0115392', 'Titanic', '1996', '87', 'Action+Drama+History' ],
        [ 'tt0120338', 'Titanic', '1997', '194', 'Drama+Romance' ],
        [ 'tt0155274', 'Titanic', '1915', '', 'History' ],
        [ 'tt0594950', 'Titanic', '1997', '', 'Documentary+Short' ],
        [ 'tt0771984', 'Titanic', '2006', '51', 'Documentary' ],
        [ 'tt0902058', 'The Titanic', '1981', '', 'Documentary+Drama+Fantasy' ],
        [ 'tt0650185', 'Titanic Tech', '2003', '46', 'Documentary+History' ],
        [ 'tt0929378', 'Titanic\'s Ghosts', '2002', '57', 'Documentary+History+War' ],
    ],
};
