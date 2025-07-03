package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.AdjustMaxSetException;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.example.ExampleDecision.DecisionColumnStatus;
import de.uni.passau.core.model.ColumnSet;

/**
 * Compute new maximal set based on the previous one and expert's decisions.
 */
public class AdjustMaxSet {

    public static MaxSet run(
            /** The previous version of the max set. */
            MaxSet prev,
            /** Example rows, preferably with the expert's decisions. */
            List<ExampleRow> exampleRows) {
        try {
            final var algorithm = new AdjustMaxSet(prev, exampleRows);
            return algorithm.innerRun();
        } catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private final MaxSet prev;
    private final List<ExampleRow> exampleRows;

    private AdjustMaxSet(MaxSet prev, List<ExampleRow> exampleRows) {
        this.prev = prev;
        this.exampleRows = exampleRows;
    }

    private MaxSet innerRun() {
        MaxSet newMaxSet = prev.clone();
        int forClass = prev.getForClass();
        for (ExampleRow row : exampleRows) {
            ColumnSet rhs = row.rhsSet;
            if (!rhs.get(forClass)) {
                // If the class is not in the rhsSet, we can skip the row
                // TODO: should this be an error?
                continue;
            }

            if (row.decision.columns()[forClass].status() == DecisionColumnStatus.VALID) {
                /* If the column C is marked as VALID, the FD lhsSet -> C does not hold.
                 * This means lhsSet (or a superset of it) is part of the max set.
                */
                newMaxSet.addCombination(row.lhsSet);
            } else {
                // ignore INVALID and UNANSWERED columns for now
                continue;
            }
        }
        // Remove combinations that are subsets of any other combination
        newMaxSet.finalize_RENAME_THIS();
        return newMaxSet;
    }

}
