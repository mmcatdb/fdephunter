package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

public class ExtendMaxSet {

    public static MaxSets run(
        MaxSets maxSets,
        int lhsSize
    ) {
        try {
            final var algorithm = new ExtendMaxSet(maxSets, lhsSize);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ExtendMaxSetException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final int lhsSize;

    private ExtendMaxSet(MaxSets maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;
    }

    private MaxSets innerRun() throws Exception {
        int numberOfColumns = maxSets.sets().size();

        List<MaxSet> prepopulatedList = new ArrayList<>(numberOfColumns);
        for (int i = 0; i < numberOfColumns; i++) {
            prepopulatedList.add(new MaxSet(i));
        }
        MaxSets extendedMaxSets = new MaxSets(prepopulatedList);

        for (MaxSet maxSet : this.maxSets.sets()) {
            int forClass = maxSet.forClass;

            // Get all MaxSets for the class with size lhsSize - 1
            MaxSet maxSetsToCheck = new MaxSet(forClass);
            for (ColumnSet columnSet : maxSet.combinations()) {
                extendedMaxSets.sets().add(forClass, maxSetsToCheck);
                if (columnSet.size() < this.lhsSize) {                       
                    maxSetsToCheck.addCombination(columnSet);
                }
            }

            /* If there are no max sets for the class with size lhsSize - 1, continue
             * In this case, all minimal FDs with size lhsSize - 1 are already confirmed
             * and all LHSs with size >= lhsSize are already in the max set.
            */
            if (maxSetsToCheck.combinations().isEmpty()) {
                continue;
            }

            /* Count for each ColumnSet wiht size lhsSize, from how many columns it can be generated
             * by adding one column to a column in maxSetsToCheck (i.e., to elements of the Max Set with size lhsSize - 1).
             * All ColumnSets for which this count is equal to lhsSize are candidates for the max set.
             * The key is the ColumnSet, the value is the count.
             */
            Map<ColumnSet, Integer> extensionMap = new HashMap<>();
            for (int i=0; i < numberOfColumns; i++) {
                // Skip the class column
                if (i == forClass) {
                    continue;
                }
                // Add all combinations of size lhsSize - 1 to the confirmed max set
                for (ColumnSet columnSet : maxSetsToCheck.combinations()) {
                    if (columnSet.get(i)) {
                        // Skip the column if it is already in the column set
                        continue;
                    }

                    ColumnSet newColumnSet = columnSet.clone();
                    newColumnSet.set(i); // Add the column to the column set
                    if (!extensionMap.containsKey(newColumnSet)) {
                        extensionMap.put(newColumnSet, 1);
                    } else {
                        extensionMap.put(newColumnSet, extensionMap.get(newColumnSet) + 1);
                    }
                }
            }
            /* Now we have a map of all ColumnSets with size lhsSize, and the number of columns
             * from which they can be generated. If the count is equal to lhsSize, it means that
             * they are candidates for the Max Set.
             */
            for (Map.Entry<ColumnSet, Integer> entry : extensionMap.entrySet()) {
                int count = entry.getValue();
                if (count == this.lhsSize) {
                    entry.getKey();
                    // Add the ColumnSet as a candidate to the extended max set
                    extendedMaxSets.sets().get(forClass).addCandidate(entry.getKey());
                }
            }
            extendedMaxSets.sets().get(forClass).finalize_RENAME_THIS();
        }
        return extendedMaxSets;
    }

}
//
