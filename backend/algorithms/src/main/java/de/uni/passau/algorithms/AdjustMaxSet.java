package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.AdjustMaxSetException;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.example.ExampleDecision.DecisionColumnStatus;

/**
 * Compute new maximal set based on the previous one and expert's decisions.
 */
public class AdjustMaxSet {

    public static MaxSets run(
        /** The previous version of the max set. */
        MaxSets prev,
        /** Example rows, preferably with the expert's decisions. */
        List<ExampleRow> exampleRows
    ) {
        try {
            final var algorithm = new AdjustMaxSet(prev, exampleRows);
            return algorithm.innerRun();
        } catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private final MaxSets prev;
    private final List<ExampleRow> exampleRows;

    private AdjustMaxSet(MaxSets prev, List<ExampleRow> exampleRows) {
        this.prev = prev;
        this.exampleRows = exampleRows;
    }

    private MaxSets innerRun() {
        final var newSets = prev.sets().stream()
            .map(MaxSet::clone)
            .toList();

        final MaxSets newMaxSets = new MaxSets(newSets);
        for (ExampleRow row : exampleRows) {
            for (int forClass : row.rhsSet.toIntList()) {
                final MaxSet newMaxSet = newMaxSets.sets().get(forClass);

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
        }

        // Remove combinations that are subsets of any other combination
        for (final var newMaxSet : newMaxSets.sets())
            newMaxSet.finalize_RENAME_THIS();

        return newMaxSets;
    }

}
