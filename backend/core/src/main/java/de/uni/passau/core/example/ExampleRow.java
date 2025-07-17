package de.uni.passau.core.example;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class ExampleRow {

    public final String[] values;

    /**
     * The indexes of the columns that form the lhs of FDs that are violated by the example.
     * It's a candidate element of all max sets for the columns in rhsSet).
     * It's an element (or a subset of one) of all max sets for the other columns (not in lhs nor rhs).
     */
    public final ColumnSet lhsSet;

    /**
     * The indexes of the columns that needs to be evaluated. They form the rhs of FDs that are violated by the example. (The other columns are also violated, but they don't need to be evaluated.)
     * It's disjoint with lhsSet.
     * The rhsSet might change - if we obtain a new information, we might need less columns to be evaluated.
     */
    public ColumnSet rhsSet;

    /**
     * Whether it's a positive or negative example.
     * All rows created from the initially discovered FDs are positive examples. Rows created when a positive example is rejected are also positive. All others are negative.
     */
    public final boolean isPositive;

    /** If null, the row is still undecided. */
    public @Nullable ExampleDecision decision = null;

    public ExampleRow(String[] values, ColumnSet lhsSet, ColumnSet rhsSet, boolean isPositive) {
        this.values = values;
        this.lhsSet = lhsSet;
        this.rhsSet = rhsSet;
        this.isPositive = isPositive;
    }

    // Override toString for better debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExampleRow{values=")
          .append(String.join(", ", values))
          .append(", lhsSet=").append(lhsSet)
          .append(", rhsSet=").append(rhsSet)
          .append(", isPositive=").append(isPositive)
          .append(", decision=").append(decision != null ? decision.toString() : "null")
          .append("}");
        return sb.toString();   
    }

}
