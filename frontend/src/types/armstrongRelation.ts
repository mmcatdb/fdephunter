export type ArmstrongRelation = {
    /** Names of the columns. They are expected to be unique. */
    columns: string[];
    /** The rows are sorted. The first one is special. */
    rows: ArmstrongRow[];
};

export type ArmstrongRow = {
    values: string[];
    /** The indexes of the columns that form the maximal set. Undefined for the first row. */
    maximalSet: number[] | undefined;
    /** Whether it is a negative or positive example. Undefined for the first row. */
    isNegative: boolean | undefined;
};
