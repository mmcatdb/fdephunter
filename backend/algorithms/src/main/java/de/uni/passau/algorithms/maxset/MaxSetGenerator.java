package de.uni.passau.algorithms.maxset;

import java.util.ArrayList;
import java.util.List;

import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.MaxSet;

public abstract class MaxSetGenerator {

    /** Returns max sets ordered by their classes. */
    public static List<MaxSet> generateMaxSets(Iterable<AgreeSet> agreeSets, int numberOfColumns) {
        final var output = new ArrayList<MaxSet>();
        for (int forClass = 0; forClass < numberOfColumns; forClass++)
            output.add(generateMaxSet(agreeSets, forClass));

        return output;
    }

    private static MaxSet generateMaxSet(Iterable<AgreeSet> agreeSets, int forClass) {
        final MaxSet result = new MaxSet(forClass);
        for (final AgreeSet agreeSet : agreeSets) {
            if (agreeSet.get(forClass))
                continue;

            result.addElement(agreeSet.cloneColumnSet());
        }

        result.pruneSubsets();

        return result;
    }

    /** Returns complement max sets ordered by their classes. */
    public static List<ComplementMaxSet> generateComplementMaxSets(List<MaxSet> maxSets) {
        final var output = new ArrayList<ComplementMaxSet>();
        for (final MaxSet maxSet : maxSets)
            output.add(generateComplementMaxSet(maxSet, maxSets.size()));

        return output;
    }

    private static ComplementMaxSet generateComplementMaxSet(MaxSet maxSet, int numberOfColumns) {
        final ComplementMaxSet result = new ComplementMaxSet(maxSet.forClass);

        for (final ColumnSet il : maxSet.confirmedElements()) {
            final ColumnSet inverse = ColumnSet.fromRange(0, numberOfColumns);
            inverse.xor(il);
            result.addCombination(inverse);
        }

        return result;
    }

}
