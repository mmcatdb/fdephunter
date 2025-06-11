package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.entity.AssignmentNode;
import de.uni.passau.server.model.entity.AssignmentNode.AssignmentState;

public record AssignmentInfo(
    String id,
    AssignmentState state,
    int rowIndex
) implements Serializable {

    public static AssignmentInfo fromNodes(AssignmentNode assignmentNode) {
        return new AssignmentInfo(
            assignmentNode.getId(),
            assignmentNode.state,
            assignmentNode.rowIndex
        );
    }

}
