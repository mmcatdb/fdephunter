package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("workflow")
public class WorkflowEntity {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public WorkflowState state;

    public Integer iteration;

    public UUID datasetId;

    public WorkflowEntity(UUID id, WorkflowState state, Integer iteration, UUID datasetId) {
        this.id = id;
        this.state = state;
        this.iteration = iteration;
        this.datasetId = datasetId;
    }

    public static WorkflowEntity create() {
        return new WorkflowEntity(
            UUID.randomUUID(),
            WorkflowState.INITIAL_SETTINGS,
            0,
            null
        );
    }

    public static enum WorkflowState {
        /** Workflow is created, now we wait for the user to select dataset(s) and approach. */
        INITIAL_SETTINGS,
        /** Wait for the initial discovery job. */
        INITIAL_FD_DISCOVERY,
        /** The initial discovery job is finished, now we wait for the user to distribute the negative examples and to finish their evaluation. */
        NEGATIVE_EXAMPLES,
        /** Wait for the rediscovery job. */
        JOB_WAITING,
        /** We have the results. */
        FINAL;
    }

}
