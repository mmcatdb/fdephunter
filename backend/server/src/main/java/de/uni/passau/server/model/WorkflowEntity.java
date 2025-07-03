package de.uni.passau.server.model;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("workflow")
public class WorkflowEntity {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public @Nullable UUID datasetId;

    public WorkflowState state;

    public Integer iteration;

    public static WorkflowEntity create() {
        final var workflow = new WorkflowEntity();

        workflow.id = UUID.randomUUID();
        workflow.datasetId = null; // No dataset selected yet.
        workflow.state = WorkflowState.INITIAL_SETTINGS;
        workflow.iteration = 0;

        return workflow;
    }

    public static enum WorkflowState {
        /** Workflow is created, now we wait for the user to select dataset(s) and approach. */
        INITIAL_SETTINGS,
        /** Wait for the initial discovery job. */
        INITIAL_FD_DISCOVERY,
        /** The initial discovery job is finished, now we wait for the user to distribute the negative examples and to finish their evaluation. */
        NEGATIVE_EXAMPLES,
        /** Wait for adjusting max set and generating examples. */
        POSITIVE_EXAMPLES,
        /** We have the results. */
        FINAL;
    }

}
