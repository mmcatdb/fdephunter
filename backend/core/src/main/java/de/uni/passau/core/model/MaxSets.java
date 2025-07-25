package de.uni.passau.core.model;

import java.io.Serializable;
import java.util.List;

// The result of an algorithm has to be an object so that it can be properly serialized to a document.
public record MaxSets(
    /** On index i, there is a max set for the class corresponding to the i-th column. */
    List<MaxSet> sets
) implements Serializable {

    @Override public String toString() {
        final var sb = new StringBuilder();
        sb.append("Max sets:\n");
        for (final var set : sets)
            sb.append("    ").append(set).append("\n");
        return sb.toString();
    }

}
