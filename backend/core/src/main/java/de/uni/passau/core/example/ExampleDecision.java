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
        /** If null, the column isn't a part of the max set (so it should be ignored). */
        @Nullable DecisionColumnStatus status,
        /** User provided strings. Probably not important right now. */
        List<String> reasons
    ) implements Serializable {}

    public enum DecisionColumnStatus {
        UNDECIDED,
        VALID,
        INVALID,
    }

}
