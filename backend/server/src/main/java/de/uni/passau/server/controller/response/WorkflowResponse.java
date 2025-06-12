package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;

public record WorkflowResponse(
    String id,
    WorkflowState state,
    int iteration
) implements Serializable {

    public static WorkflowResponse fromNodes(WorkflowNode workflowNode) {
        return new WorkflowResponse(
            workflowNode.getId(),
            workflowNode.state,
            workflowNode.iteration
        );
    }

}
