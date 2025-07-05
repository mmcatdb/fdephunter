package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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

    private ExtendMaxSets(MaxSets maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;
    }

    private MaxSets innerRun() {
        final int numberOfColumns = maxSets.sets().size();
        final MaxSets extendedMaxSets = new MaxSets(new ArrayList<>());

        for (final MaxSet original : maxSets.sets()) {
            final MaxSet extended = extendMaxSet(original, numberOfColumns);
            extendedMaxSets.sets().add(extended);
        }

        return extendedMaxSets;
    }

    private MaxSet extendMaxSet(MaxSet original, int numberOfColumns) {
        // Get all MaxSets for the class with size lhsSize - 1
        final MaxSet extended = new MaxSet(original.forClass);

        for (final ColumnSet columnSet : original.confirmedElements()) {
            if (columnSet.size() < lhsSize)
                extended.addElement(columnSet);
        }

        // If there are no max sets for the class with size lhsSize - 1, continue
        // In this case, all minimal FDs with size lhsSize - 1 are already confirmed
        // and all LHSs with size >= lhsSize are already in the max set.
        //
        // TODO: Check whether we are completely done with this class,
        //  i.e., whether there is any element in maxSet with size >= (or >?) lhsSize
        if (extended.confirmedCount() == 0)
            return extended;

        // TODO: if extended.size() < lhsSize, we can skip the rest

        final Map<ColumnSet, Integer> extensionMap = createExtensionMap(extended, numberOfColumns);

        // Now we have a map of all ColumnSets with size lhsSize, and the number of columns
        // from which they can be generated. If the count is equal to lhsSize, it means that
        // they are candidates for the Max Set.
        for (final Map.Entry<ColumnSet, Integer> entry : extensionMap.entrySet()) {
            final int count = entry.getValue();
            if (count == lhsSize) {
                entry.getKey();
                // Add the ColumnSet as a candidate to the extended max set
                extended.addCandidate(entry.getKey());
            }
        }

        extended.pruneSubsets();

        return extended;
    }

    /**
     * Count for each ColumnSet wiht size lhsSize, from how many columns it can be generated
     * by adding one column to a column in extended max set (i.e., to elements of the max Set with size lhsSize - 1).
     * All ColumnSets for which this count is equal to lhsSize are candidates for the max set.
     * The key is the ColumnSet, the value is the count.
     */
    private Map<ColumnSet, Integer> createExtensionMap(MaxSet extended, int numberOfColumns) {
        final Map<ColumnSet, Integer> output = new HashMap<>();

        for (int i = 0; i < numberOfColumns; i++) {
            // Skip the class column
            if (i == extended.forClass)
                continue;

            // Add all combinations of size lhsSize - 1 to the confirmed max set
            for (final ColumnSet columnSet : extended.confirmedElements()) {
                if (columnSet.get(i)) {
                    // Skip the column if it is already in the column set
                    continue;
                }

                final ColumnSet newColumnSet = columnSet.clone();
                newColumnSet.set(i); // Add the column to the column set

                final var current = output.get(newColumnSet);
                output.put(newColumnSet, current == null ? 1 : current + 1);
            }
        }

        return output;
    }

}
