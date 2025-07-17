package de.uni.passau.core.example;

import java.io.Serializable;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents the user decision for an example.
 * The whole class is immutable - only the user can evaluate / re-evaluate the example.
 */
public record ExampleDecision(
    DecisionStatus status,
    /** The columns are in the same order as in the original relation. */
    DecisionColumn[] columns
) implements Serializable {

    public enum DecisionStatus {
        ACCEPTED,
        REJECTED,
        UNANSWERED,
    }

    public record DecisionColumn(
        // NICE_TO_HAVE In that case, the whole decision column should be null, right?
        /** If null, the column isn't a part of the example row's rhsSet (so it should be ignored). */
        @Nullable DecisionColumnStatus status,
        /** User provided strings. Probably not important right now. */
        List<String> reasons
    ) implements Serializable {}

    public enum DecisionColumnStatus {
        VALID,
        INVALID,
        UNDECIDED,
    }

    // Implement toString for better debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExampleDecision{status=").append(status).append(", columns=[");
        for (DecisionColumn column : columns) {
            // If the column is null, we skip it
            if (column == null) {
                sb.append("null, ");
                continue;
            }
            // Otherwise, we append the column's status and reasons
            sb.append("{status=").append(column.status).append(", reasons=").append(column.reasons).append("}, ");
        }
        sb.append("]}");
        return sb.toString();   
    }
}
