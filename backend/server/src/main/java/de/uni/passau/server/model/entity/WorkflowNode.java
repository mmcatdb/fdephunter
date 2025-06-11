package de.uni.passau.server.model.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Workflow")
public class WorkflowNode {

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

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public WorkflowState state;

    @Property
    public Integer iteration;

    public static WorkflowNode createNew() {
        final var workflow = new WorkflowNode();
        workflow.state = WorkflowState.INITIAL_SETTINGS;
        workflow.iteration = 0;

        return workflow;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WorkflowNode{");
        sb.append("id=").append(id);
        sb.append(", state=").append(state);
        sb.append(", iteration=").append(iteration);
        sb.append('}');
        return sb.toString();
    }

}
