package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;

public record NegativeExampleInfo(
    String id,
    NegativeExampleState state
) implements Serializable {

    public static NegativeExampleInfo fromNodes(NegativeExampleNode exampleNode) {
        return new NegativeExampleInfo(
            exampleNode.getId(),
            exampleNode.state
        );
    }

}
