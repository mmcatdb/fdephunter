package de.uni.passau.server.clientdto;

import de.uni.passau.server.workflow.model.DiscoveryJobNode;
import de.uni.passau.server.workflow.model.DiscoveryJobNode.DiscoveryJobState;

import java.io.Serializable;

public record DiscoveryJob(
    String id,
    DiscoveryJobState state,
    String description,
    int iteration
) implements Serializable {
    
    public static DiscoveryJob fromNodes(DiscoveryJobNode jobNode) {
        return new DiscoveryJob(
            jobNode.getId(),
            jobNode.getState(),
            jobNode.getDescription(),
            jobNode.getIteration()
        );
    }

}
