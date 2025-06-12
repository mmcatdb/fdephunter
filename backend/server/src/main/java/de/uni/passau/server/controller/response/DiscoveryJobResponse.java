package de.uni.passau.server.controller.response;

import java.io.Serializable;
import java.util.Date;

import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;

public record DiscoveryJobResponse(
    String id,
    DiscoveryJobState state,
    String description,
    int iteration,
    Date startedAt
) implements Serializable {

    public static DiscoveryJobResponse fromNodes(DiscoveryJobNode jobNode) {
        return new DiscoveryJobResponse(
            jobNode.getId(),
            jobNode.state,
            jobNode.description,
            jobNode.iteration,
            jobNode.startedAt
        );
    }

}
