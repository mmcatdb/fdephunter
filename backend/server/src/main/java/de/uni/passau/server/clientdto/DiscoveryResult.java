package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.DiscoveryResultNode;

import java.io.Serializable;

public record DiscoveryResult(
    String id,
    String payload
) implements Serializable {

    public static DiscoveryResult fromNodes(DiscoveryResultNode resultNode) {
        return new DiscoveryResult(
            resultNode.getId(),
            resultNode.getPayload()
        );
    }

}
