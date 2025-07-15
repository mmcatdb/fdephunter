package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

public class ExtendMaxSets {

    public static MaxSets run(
        MaxSets maxSets,
        int lhsSize
    ) {
        try {
            final var algorithm = new ExtendMaxSets(maxSets, lhsSize);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ExtendMaxSetException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final int lhsSize;

    private final int numberOfColumns;

    private ExtendMaxSets(MaxSets maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;

        this.numberOfColumns = maxSets.sets().size();
    }

    private MaxSets innerRun() {
        final MaxSets extendedMaxSets = new MaxSets(new ArrayList<>());

        for (final MaxSet original : maxSets.sets()) {
            // Clone the original max set because we don't want to modify it.
            final MaxSet extended = original.clone();
            if (lhsSize == 1)
                extendMaxSetForSizeOne(extended);
            else
                extendMaxSet(extended);

            extendedMaxSets.sets().add(extended);
        }

        return extendedMaxSets;
    }

    private void extendMaxSetForSizeOne(MaxSet extended) {
        // This is a special case because the one-element elements that we want to generate don't have any subsets.
        for (int i = 0; i < numberOfColumns; i++) {
            if (i == extended.forClass)
                continue; // Skip the class column

            // Add the column as a candidate to the extended max set (only if it's not a confirmed element already).
            final ColumnSet columnSet = ColumnSet.fromIndexes(i);
            if (!extended.hasConfirmed(columnSet))
                extended.addCandidate(columnSet);
        }

        extended.pruneSubsets();
    }

    /**
     * Get a list of all possible subsets of the extended max set with size lhsSize - 1.
     * For example if the extended max set is {1, 2, 3}, the subsets will be {1, 2}, {1, 3}, {2, 3}.
     * This is used to generate new candidates for the max set.
     * @param extended The extended max set.
     * @return A list of all subsets of the extended max set with size lhsSize - 1.
     */
    private List<ColumnSet> getSubsets(MaxSet extended) {
        final HashSet<ColumnSet> subsets = new HashSet<>();
        for (final ColumnSet element : extended.elements().toList()) {
            if (element.size() == lhsSize - 1) {
                subsets.add(element);
            }
            // if (element.size() > lhsSize - 1)  we need to split the element into all possible subsets of size lhsSize - 1
            else if (element.size() > lhsSize - 1) {
                // Generate all subsets of size lhsSize - 1
                final int[] indexes = element.toIndexes();
                for (int i = 0; i < indexes.length; i++) {
                    final int[] subsetIndexes = new int[lhsSize - 1];
                    int indexCounter = 0;
                    for (int j = 0; j < indexes.length; j++) {
                        if (j != i) { // Skip the current index
                            subsetIndexes[indexCounter++] = indexes[j];
                        }
                    }
                    subsets.add(ColumnSet.fromIndexes(subsetIndexes));
                }
            }
        }
        return subsets.stream().toList();
    }

    /**
     * Prune candidates from the extended max set.
     * This is done by removing candidates that are already confirmed in the extended max set.
     * @param extended The extended max set.
     */
    private void pruneCandidates(MaxSet extended) {
        final List<ColumnSet> candidatesToRemove = new ArrayList<>();
        for (final ColumnSet candidate : extended.candidates().toList()) {
            if (extended.hasConfirmed(candidate)) {
                candidatesToRemove.add(candidate);
            }
        }
        for (final ColumnSet candidate : candidatesToRemove) {
            extended.removeCandidate(candidate);
        }
    }

    private void extendMaxSet(MaxSet extended) {
        // Get all MaxSets for the class with size lhsSize - 1.
        //final var subsets = extended.elements().filter(element -> element.size() == lhsSize - 1).toList();
        final List<ColumnSet> subsets = getSubsets(extended);

        // If there are no max sets for the class with size lhsSize - 1, continue
        // In this case, all minimal FDs with size lhsSize - 1 are already confirmed
        // and all LHSs with size >= lhsSize are already in the max set.
        //
        // TODO: Check whether we are completely done with this class,
        // I.e., whether there is any element in maxSet with size >= (or >?) lhsSize
        if (subsets.size() == 0)
            return;

        // TODO: if extended.size() < lhsSize, we can skip the rest

        final Map<ColumnSet, Integer> extensionMap = createExtensionMap(extended.forClass, subsets);

        // Now we have a map of all ColumnSets with size lhsSize, and the number of columns from which they can be generated.
        // If the count is equal to lhsSize, it means that they are candidates for the Max Set.
        for (final Entry<ColumnSet, Integer> entry : extensionMap.entrySet()) {
            final int count = entry.getValue();
            if (count == lhsSize) {
                entry.getKey();
                // Add the ColumnSet as a candidate to the extended max set
                extended.addCandidate(entry.getKey());
            }
        }

        // Remove candidates that are already confirmed in the extended max set.
        pruneCandidates(extended);

        extended.pruneSubsets();
    }

    /**
     * Count for each ColumnSet wiht size lhsSize, from how many columns it can be generated
     * by adding one column to a column in extended max set (i.e., to elements of the max Set with size lhsSize - 1).
     * All ColumnSets for which this count is equal to lhsSize are candidates for the max set.
     * The key is the ColumnSet, the value is the count.
     */
    private Map<ColumnSet, Integer> createExtensionMap(int forClass, List<ColumnSet> subsets) {
        final Map<ColumnSet, Integer> output = new HashMap<>();

        for (int i = 0; i < numberOfColumns; i++) {
            // Skip the class column
            if (i == forClass)
                continue;

            // Add all combinations of size lhsSize - 1 to the confirmed max set.
            for (final ColumnSet columnSet : subsets) {
                if (columnSet.get(i)) {
                    // Skip the column if it's already in the column set.
                    continue;
                }

                final ColumnSet newColumnSet = columnSet.clone();
                newColumnSet.set(i); // Add the column to the column set.

                final var current = output.get(newColumnSet);
                output.put(newColumnSet, current == null ? 1 : current + 1);
            }
        }

        return output;
    }

}
