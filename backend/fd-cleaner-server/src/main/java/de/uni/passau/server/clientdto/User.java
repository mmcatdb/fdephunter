package de.uni.passau.server.clientdto;

import de.uni.passau.server.workflow.model.UserNode;

import java.io.Serializable;

public record User(
    String id,
    String firstName,
    String lastName,
    String email    
) implements Serializable {

    public static User fromNodes(UserNode userNode) {
        return new User(
            userNode.getId(),
            userNode.getFirstName(),
            userNode.getLastName(),
            userNode.getEmail()
        );
    }
    
}
