package de.uni.passau.server.controller.response;

import java.io.Serializable;
import java.util.Date;

import de.uni.passau.server.model.entity.DiscoveryJobNode;
import de.uni.passau.server.model.entity.DiscoveryJobNode.DiscoveryJobState;

public record DiscoveryJob(
    String id,
    DiscoveryJobState state,
    String description,
    int iteration,
    Date startedAt
) implements Serializable {

    public static DiscoveryJob fromNodes(DiscoveryJobNode jobNode) {
        return new DiscoveryJob(
            jobNode.getId(),
            jobNode.state,
            jobNode.description,
            jobNode.iteration,
            jobNode.startedAt
        );
    }

}
