package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;

public record Workflow(
    String id,
    WorkflowState state,
    int iteration
) implements Serializable {

    public static Workflow fromNodes(WorkflowNode workflowNode) {
        return new Workflow(
            workflowNode.getId(),
            workflowNode.state,
            workflowNode.iteration
        );
    }

}
