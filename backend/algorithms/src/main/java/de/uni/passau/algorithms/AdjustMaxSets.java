package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.AdjustMaxSetException;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.example.ExampleDecision.DecisionColumnStatus;

/**
 * Compute new maximal set based on the previous one and expert's decisions.
 */
public class AdjustMaxSets {

    /**
     * @param prev - The previous version of the max set.
     * @param exampleRows - Example rows, preferably with the expert's decisions.
     */
    public static MaxSets run(
        MaxSets prev,
        List<ExampleRow> exampleRows,
        boolean isEvaluatingPositives
    ) {
        try {
            final var algorithm = new AdjustMaxSets(prev, exampleRows, isEvaluatingPositives);
            return algorithm.innerRun();
        } catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private final MaxSets prev;
    private final List<ExampleRow> exampleRows;
    private final boolean isEvaluatingPositives;

    private AdjustMaxSets(MaxSets prev, List<ExampleRow> exampleRows, boolean isEvaluatingPositives) {
        this.prev = prev;
        this.exampleRows = exampleRows;
        this.isEvaluatingPositives = isEvaluatingPositives;
    }

    private MaxSets innerRun() {
        final var newSets = prev.sets().stream()
            .map(MaxSet::clone)
            .toList();

        for (final ExampleRow row : exampleRows) {
            for (final int forClass : row.rhsSet.toIndexes()) {
                final MaxSet newMaxSet = newSets.get(forClass);

                if (row.decision.columns()[forClass].status() == DecisionColumnStatus.VALID) {
                    // If the column C is marked as VALID, the FD lhsSet -> C does not hold.
                    // This means lhsSet (or a superset of it) is part of the max set.
                    //
                    // The lhsSet should be a candidate in the previous max set, we can safely move it to the true max set.
                    newMaxSet.moveCandidateToConfirmeds(row.lhsSet);
                } else if (row.decision.columns()[forClass].status() == DecisionColumnStatus.INVALID) {
                    // If the column C is marked as INVALID, the FD lhsSet -> C holds.
                    // This means lhsSet (or a superset of it) is not part of the max set.
                    //
                    // The lhsSet should be a candidate in the previous max set and we remove it.
                    newMaxSet.removeCandidate(row.lhsSet);

                    // Reconstruct the "child" max sets, i.e., any subset of lhsSet that has exactly one column less than lhsSet.
                    for (final int i: row.lhsSet.toIndexes()) {
                        if (row.lhsSet.size() == 1)
                            continue; // We don't want to create subsets of size 0.

                        final ColumnSet subset = row.lhsSet.clone();
                        subset.clear(i);

                        // We can simply add the subset to the new max set, it will be checked later.
                        if (isEvaluatingPositives) {
                            // The evaluated element was rejected, so it's subsets might be invalid as well.
                            // We should check them in the next iteration, so we add them as candidates.
                            newMaxSet.addCandidate(subset);
                        }
                        else {
                            // The evaluated element was rejected, but it was based on confirmed elements, so we can add them back to the confimeds.
                            newMaxSet.addConfirmed(subset);
                        }

                        // NICE_TO_HAVE We may be smarter and not add elements that we will just remove later.
                    }
                } else {
                    // ignore UNANSWERED columns (for now)
                    continue;
                }

                newMaxSet.pruneSubsets();
            }
        }

        return new MaxSets(newSets);
    }

}
