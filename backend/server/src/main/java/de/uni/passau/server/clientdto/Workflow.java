package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;

import java.io.Serializable;

public record Workflow(
    String id,
    WorkflowState state,
    int iteration
) implements Serializable {

    public static Workflow fromNodes(WorkflowNode workflowNode) {
        return new Workflow(
            workflowNode.getId(),
            workflowNode.getState(),
            workflowNode.getIteration()
        );
    }

}
