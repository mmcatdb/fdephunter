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
    public @Nullable ExampleDecision decision;

    public ExampleRow(String[] values, ColumnSet lhsSet, ColumnSet rhsSet, boolean isPositive) {
        this.values = values;
        this.lhsSet = lhsSet;
        this.rhsSet = rhsSet;
        this.isPositive = isPositive;
        this.decision = null;
        if (lhsSet == null || rhsSet == null) {
            throw new IllegalArgumentException("lhsSet and rhsSet must not be null.");
        }
        if (lhsSet.isEmpty() || rhsSet.isEmpty()) {
            throw new IllegalArgumentException("lhsSet and rhsSet must not be empty.");
        }
        if (lhsSet.intersects(rhsSet)) {
            throw new IllegalArgumentException("lhsSet and rhsSet must be disjoint.");
        }
    }

    public void setDecision(ExampleDecision decision) {
        if (decision == null) {
            this.decision = null;
            return;
        }
        // Every column in rhsSet needs to have a decision (and no other column).
        int[] decisionColumnsArray = java.util.stream.IntStream.range(0, decision.columns().length)
            .filter(i -> decision.columns()[i].status() != null)
            .toArray();
        if (!rhsSet.equals(ColumnSet.fromIndexes(decisionColumnsArray))){
            throw new IllegalArgumentException("Decision columns must match the rhsSet.");
        }

        this.decision = decision;
    }


}
