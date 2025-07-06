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
        List<ExampleRow> exampleRows
    ) {
        try {
            final var algorithm = new AdjustMaxSets(prev, exampleRows);
            return algorithm.innerRun();
        } catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private final MaxSets prev;
    private final List<ExampleRow> exampleRows;

    private AdjustMaxSets(MaxSets prev, List<ExampleRow> exampleRows) {
        this.prev = prev;
        this.exampleRows = exampleRows;
    }

    private MaxSets innerRun() {
        final var newSets = prev.sets().stream()
            .map(MaxSet::clone)
            .toList();

        final MaxSets newMaxSets = new MaxSets(newSets);
        for (final ExampleRow row : exampleRows) {
            for (final int forClass : row.rhsSet.toIndexes()) {
                final MaxSet newMaxSet = newMaxSets.sets().get(forClass);

                if (row.decision.columns()[forClass].status() == DecisionColumnStatus.VALID) {
                    // If the column C is marked as VALID, the FD lhsSet -> C does not hold.
                    // This means lhsSet (or a superset of it) is part of the max set.
                    //
                    // lhsSet should be a candidate in the previous max set and we move it to the
                    // true max set.
                    newMaxSet.moveToTrueMaxSet(row.lhsSet);
                } else if (row.decision.columns()[forClass].status() == DecisionColumnStatus.INVALID) {
                    // If the column C is marked as INVALID, the FD lhsSet -> C holds.
                    // This means lhsSet (or a superset of it) is not part of the max set.
                    //
                    // lhsSet should be a candidate in the previous max set and we remove it.
                    newMaxSet.removeCandidate(row.lhsSet);

                    // Reconstruct the "child" max sets, i.e., any subset of lhsSet that has exactly
                    // one column less than lhsSet
                    for (final int i: row.lhsSet.toIndexes()) {
                        final ColumnSet subset = row.lhsSet.clone();
                        subset.clear(i);
                        // Simply add the subset to the new max set; it will be checked later
                        // TODO: We may be smarter and not add elements that we will just remove later.
                        newMaxSet.addElement(subset);
                    }
                } else {
                    // ignore UNANSWERED columns (for now)
                    continue;
                }

                newMaxSet.pruneSubsets();
            }
        }

        return newMaxSets;
    }

}
