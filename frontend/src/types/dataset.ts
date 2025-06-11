export enum DatasetType {
    Csv = 'CSV',
    Json = 'JSON',
    Xml = 'XML',
    LabeledGraph = 'LABELED_GRAPH',
    Relational = 'RELATIONAL',
    Array = 'ARRAY',
    Rdf = 'RDF',
}

export type DatasetResponse = {
    name: string;
    type: DatasetType;
    // NICE_TO_HAVE not important now
    // source: string;
    // kindName: string;
    // columns: number;
    // rows: number;
    // size: number;
    // fds: number;
};

export type DatasetHeader = string[];

export type DatasetRow = string[];

export type DatasetData = {
    header: DatasetHeader;
    rows: DatasetRow[];
};
