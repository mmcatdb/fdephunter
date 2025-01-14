package de.uni.passau.server.clientdto;

import de.uni.passau.server.workflow.model.AssignmentNode;
import de.uni.passau.server.workflow.model.ExpertNode;
import de.uni.passau.server.workflow.model.ExpertNode.ExpertState;
import de.uni.passau.server.workflow.model.UserNode;
import de.uni.passau.server.workflow.repository.ExpertRepository.ExpertNodeGroup;

import java.io.Serializable;

import org.springframework.lang.Nullable;

public record Expert(
    String id,
    ExpertState state,
    User user,
    @Nullable AssignmentInfo assignment
) implements Serializable {

    public static Expert fromNodes(ExpertNode expertNode, UserNode userNode, AssignmentNode assignmentNode) {
        return new Expert(
            expertNode.getId(),
            expertNode.getState(),
            User.fromNodes(userNode),
            assignmentNode != null ? AssignmentInfo.fromNodes(assignmentNode) : null
        );
    }

    public static Expert fromNodes(ExpertNodeGroup group) {
        return Expert.fromNodes(group.expert(), group.user(), group.assignment());
    }

}
