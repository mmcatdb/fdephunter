package de.uni.passau.algorithms.maxset;

import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class TupleEquivalenceClassRelation {

    /**
     * Map indexed by column index.
     * If set on index i contains j, then there is a relationship i -> j.
     */
    private final Int2ObjectMap<IntSet> equivalenceClasses = new Int2ObjectOpenHashMap<IntSet>();

    public void addRelationship(int forClass, int equivalenceClass) {
        final var set = equivalenceClasses.computeIfAbsent(forClass, x -> new IntOpenHashSet());
        set.add(equivalenceClass);
    }

    public void intersectWithAndAddToAgreeSet(TupleEquivalenceClassRelation other, Set<AgreeSet> agreeSets) {
        final AgreeSet set = new AgreeSet();

        for (final int forClass : equivalenceClasses.keySet()) {
            final var otherClasses = other.equivalenceClasses.get(forClass);
            if (otherClasses == null)
                continue; // No intersection possible for this class.

            for (final int equivalenceClass : equivalenceClasses.get(forClass)) {
                if (otherClasses.contains(equivalenceClass)) {
                    set.set(forClass);
                    break; // No need to check further for this class.
                }
            }
        }

        if (!set.isEmpty())
            agreeSets.add(set); // Add the set only if there is at least one intersection.
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (final int forClass : equivalenceClasses.keySet()) {
            for (final int equivalenceClass : equivalenceClasses.get(forClass)) {
                sb.append("(").append(forClass).append(", ").append(equivalenceClass).append("), ");
            }
        }
        if (equivalenceClasses.size() > 0)
            sb.setLength(sb.length() - 2); // Remove last comma and space.

        return sb.toString();
    }

}
