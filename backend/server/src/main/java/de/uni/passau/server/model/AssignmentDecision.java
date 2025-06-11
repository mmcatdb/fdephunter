package de.uni.passau.server.model;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class AssignmentDecision {

    public DecisionStatus status;
    public DecisionColumn[] columns;

    public enum DecisionStatus {
        ACCEPTED,
        REJECTED,
        UNANSWERED,
    }

    public record DecisionColumn(
        String name,
        @Nullable DecisionColumnStatus status,
        List<String> reasons
    ) {}

    public enum DecisionColumnStatus {
        UNDECIDED,
        VALID,
        INVALID,
    }

}
