package de.uni.passau.core.model;

import java.util.List;

/**
 * A set of all functional dependencies (FDs) in a dataset.
 * Used primarily as an overview for the user.
 */
public class FdSet {

    /** Names of the columns. They are expected to be unique. */
    String[] columns;
    /**
     * For each column index i, there is a list of all column sets that when put on the lhs they form a functional dependency (with i on the rhs).
     * All numbers are indexes to the {@link #columns} array.
     */
    List<List<ColumnSet>> fdClasses;

}
