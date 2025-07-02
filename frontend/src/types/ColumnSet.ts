/**
 * Represents a non-empty set of columns.
 */
export type ColumnSet = {
    // FIXME use bitset (or at least transform from it).
    /** Indexes of the columns. */
    columns: number[];
}
