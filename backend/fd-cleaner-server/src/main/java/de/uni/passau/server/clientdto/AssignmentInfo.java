package de.uni.passau.server.clientdto;

import de.uni.passau.server.workflow.model.AssignmentNode;
import de.uni.passau.server.workflow.model.AssignmentNode.ExpertVerdict;

import java.io.Serializable;

public record AssignmentInfo(
    String id,
    ExpertVerdict verdict
) implements Serializable {

    public static AssignmentInfo fromNodes(AssignmentNode assignmentNode) {
        return new AssignmentInfo(
            assignmentNode.getId(),
            assignmentNode.getVerdict()
        );
    }

}
