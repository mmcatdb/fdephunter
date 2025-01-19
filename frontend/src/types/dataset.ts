import { type Assignment } from './assignment';

enum DatasetType {
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

export type DatasetDataWithExamples = DatasetData & {
    examples: DatasetRow[]; // TODO remove
}

export function createDataWithExamples(assignment: Assignment): DatasetDataWithExamples {
    const dataset = assignment.dataset;

    return {
        ...dataset,
        examples: [ assignment.example.toRow(dataset.header) ],
    };
}
