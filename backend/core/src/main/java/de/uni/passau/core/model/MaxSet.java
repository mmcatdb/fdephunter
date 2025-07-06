package de.uni.passau.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MaxSet implements Cloneable {

    /** Index of the RHS column. */
    public final int forClass;
    /** Elements that are confirmed to be in the max set (at least in the current version of max set). */
    private final Set<ColumnSet> confirmeds = new HashSet<>();
    /** Elements that are speculatively added to the max set, but might be removed later (or moved to the confirmed category). */
    private final Set<ColumnSet> candidates = new HashSet<>();

    private MaxSet(int forClass, Iterable<ColumnSet> confirmeds, Iterable<ColumnSet> candidates) {
        this.forClass = forClass;

        for (final ColumnSet confirmed : confirmeds)
            this.confirmeds.add(confirmed);

        for (final ColumnSet candidate : candidates)
            this.candidates.add(candidate);
    }

    public MaxSet(int forClass) {
        this(forClass, List.of());
    }

    /** Mostly for tests. */
    public MaxSet(int forClass, Iterable<ColumnSet> confirmeds) {
        this(forClass, confirmeds, List.of());
    }

    @Override public MaxSet clone() {
        return new MaxSet(forClass, confirmeds, candidates);
    }

    public Iterable<ColumnSet> confirmedElements() {
        return confirmeds;
    }

    public int confirmedCount() {
        return confirmeds.size();
    }

    public Stream<ColumnSet> elements() {
        return Stream.concat(
            confirmeds.stream(),
            candidates.stream()
        );
    }

    public void addElement(ColumnSet element) {
        if (candidates.contains(element))
            throw new IllegalArgumentException("Confirmed element already exists in candidates: " + element + ". \nUse moveToTrueMaxSet() to move it to the true max set.");

        confirmeds.add(element);
    }

    public void addCandidate(ColumnSet candidate) {
        if (confirmeds.contains(candidate))
            throw new IllegalArgumentException("Candidate element already exists in confirmed: " + candidate);

        candidates.add(candidate);
    }

    public void removeCandidate(ColumnSet candidate) {
        if (!candidates.remove(candidate))
            throw new IllegalArgumentException("Candidate element does not exist: " + candidate);
    }

    public void moveToTrueMaxSet(ColumnSet candidate) {
        removeCandidate(candidate);
        confirmeds.add(candidate);
    }

    // We can't keep the information about calling this method because the set might be mofidied later.
    public void pruneSubsets() {
        final Set<ColumnSet> supersets = new HashSet<ColumnSet>();

        // Process both confirmed and candidate elements.
        // Create a list from the stream to avoid concurrent modification issues.
        for (final ColumnSet set : elements().toList()) {
            boolean isSubset = false;
            List<ColumnSet> supersetsToDelete = new ArrayList<>();

            for (final ColumnSet superset : supersets) {
                if (set.isSupersetOf(superset)) {
                    // All supersets that are subsets of the current set needs to be purged.
                    supersetsToDelete.add(superset);
                }
                else if (superset.isSupersetOf(set)) {
                    isSubset = true;
                    // No need to check other supersets - if the set is a subset of one of them, it can't be a superset of another.
                    break;
                }
            }

            if (isSubset)
                continue; // The set is a subset of some superset so we don't need it anymore.

            // Remove all supersets that are subsets of the current set.
            supersets.removeAll(supersetsToDelete);
            supersets.add(set);
        }

        // Now we have all supersets, so we can filter the elements.
        confirmeds.removeIf(set -> !supersets.contains(set));
        candidates.removeIf(set -> !supersets.contains(set));
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("max(").append(forClass).append(": ");
        for (final ColumnSet set : confirmeds)
            sb.append(set).append(", ");
        if (confirmeds.size() > 0)
            sb.setLength(sb.length() - 2); // Remove last comma and space.
        sb.append(")");

        return sb.toString();
    }

}
