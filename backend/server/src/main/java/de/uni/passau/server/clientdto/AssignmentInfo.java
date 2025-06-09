package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.AssignmentNode.AssignmentVerdict;

import java.io.Serializable;

public record AssignmentInfo(
    String id,
    AssignmentVerdict verdict
) implements Serializable {

    public static AssignmentInfo fromNodes(AssignmentNode assignmentNode) {
        return new AssignmentInfo(
            assignmentNode.getId(),
            assignmentNode.getVerdict()
        );
    }

}
