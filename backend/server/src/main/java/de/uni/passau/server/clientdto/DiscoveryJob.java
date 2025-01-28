package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;

import java.io.Serializable;
import java.util.Date;

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
            jobNode.getState(),
            jobNode.getDescription(),
            jobNode.getIteration(),
            jobNode.getStartedAt()
        );
    }

}
