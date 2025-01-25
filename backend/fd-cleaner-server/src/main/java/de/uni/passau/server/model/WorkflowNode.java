package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Workflow")
public class WorkflowNode {

    public static enum WorkflowState {
        /** Workflow is created, now we wait for the user to select dataset(s) and approach. */
        INITIAL,
        /** Wait for the initial discovery job. */
        INITIAL_JOB_WAITING,
        /** The initial discovery job is finished, now we wait for the user to distribute the negative examples and for the workers to finish their evaluation. */
        WORKER_ASSIGNMENT,
        /** Wait for the rediscovery job. */
        JOB_WAITING,
        /** We have the results. */
        FINAL;
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private WorkflowState state;

    @Property
    private Integer iteration;

    public static WorkflowNode createNew() {
        final var workflow = new WorkflowNode();
        workflow.setState(WorkflowState.INITIAL);
        workflow.setIteration(0);

        return workflow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public Integer getIteration() {
        return iteration;
    }

    public void setIteration(Integer iteration) {
        this.iteration = iteration;
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
