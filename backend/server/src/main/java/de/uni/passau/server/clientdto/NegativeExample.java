package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;

import java.io.Serializable;

/** @deprecated */
public record NegativeExample(
    String id,
    String payload,
    NegativeExampleState state
) implements Serializable {

    public static NegativeExample fromNodes(NegativeExampleNode exampleNode) {
        return new NegativeExample(
            exampleNode.getId(),
            exampleNode.getPayload(),
            exampleNode.getState()
        );
    }

}
