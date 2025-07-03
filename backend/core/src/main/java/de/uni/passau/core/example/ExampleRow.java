package de.uni.passau.core.example;

import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class ExampleRow {

    public String[] values;
    /** The indexes of the columns that form the left/right hand side of FDs that are violated by the example. 
     * lhsSet and rhsSet are disjoint
     * lhsSet is not a subset of any set in the max set of any of the columns in rhsSet.
     * A column X is neither in lhsSet nor in rhsSet, if lhsSet is a subset of at least one set in the max set of X.
    */
    public ColumnSet lhsSet;
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
