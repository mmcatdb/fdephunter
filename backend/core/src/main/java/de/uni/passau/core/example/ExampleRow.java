package de.uni.passau.core.example;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class ExampleRow {

    public String[] values;
    /**
     * The indexes of the columns that form the lhs of FDs that are violated by the example.
     * It's a candidate element of all max sets for the columns in rhsSet).
     * It's an element (or a subset of one) of all max sets for the other columns (not in lhs nor rhs).
     */
    public ColumnSet lhsSet;
    /**
     * The indexes of the columns that form the rhs of FDs that are violated by the example.
     * It's disjoint with lhsSet.
     */
    public ColumnSet rhsSet;

    /** Whether it is a negative or positive example. */
    public boolean isPositive;

    /** If null, the row is still undecided. */
    public @Nullable ExampleDecision decision = null;

    public ExampleRow(String[] values, ColumnSet lhsSet, ColumnSet rhsSet, boolean isPositive) {
        this.values = values;
        this.lhsSet = lhsSet;
        this.rhsSet = rhsSet;
        this.isPositive = isPositive;
    }

}
