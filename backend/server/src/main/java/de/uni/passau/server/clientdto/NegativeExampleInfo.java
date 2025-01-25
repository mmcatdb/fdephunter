package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;

import java.io.Serializable;

public record NegativeExampleInfo(
    String id,
    NegativeExampleState state
) implements Serializable {

    public static NegativeExampleInfo fromNodes(NegativeExampleNode exampleNode) {
        return new NegativeExampleInfo(
            exampleNode.getId(),
            exampleNode.getState()
        );
    }

}
