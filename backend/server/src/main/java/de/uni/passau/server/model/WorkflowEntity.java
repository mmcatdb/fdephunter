package de.uni.passau.server.model;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document("workflow")
public class WorkflowEntity {

    @Id @JsonProperty("id")
    private UUID _id;

    public UUID id() {
        return _id;
    }

    public @Nullable UUID datasetId;

    public WorkflowState state;

    /**
     * Size of the currently processed max set elements. Works for both negative and positive examples.
     * I.e., it starts with 1, then goes up to the number of columns in the dataset, and then goes back to 1.
     */
    public Integer lhsSize;

    public static WorkflowEntity create() {
        final var workflow = new WorkflowEntity();

        workflow._id = UUID.randomUUID();
        workflow.datasetId = null; // No dataset selected yet.
        workflow.state = WorkflowState.INITIAL_SETTINGS;
        workflow.lhsSize = 0;

        return workflow;
    }

    public static enum WorkflowState {
        /** Workflow is created, now we wait for the user to select the dataset. */
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

    public static String initialMaxSetsId(UUID id) { return id.toString() + "/initial-maxsets"; }
    public String initialMaxSetsId() { return WorkflowEntity.initialMaxSetsId(_id); }

    public static String maxSetsId(UUID id) { return id.toString() + "/maxsets"; }
    public String maxSetsId() { return WorkflowEntity.maxSetsId(_id); }

    public static String arId(UUID id) { return id.toString() + "/ar"; }
    public String arId() { return WorkflowEntity.arId(_id);}

    public static String latticesId(UUID id) { return id.toString() + "/lattices"; }
    public String latticesId() { return WorkflowEntity.latticesId(_id); }

    public static String fdsId(UUID id) { return id.toString() + "/fds"; }
    public String fdsId() { return WorkflowEntity.fdsId(_id); }

}
