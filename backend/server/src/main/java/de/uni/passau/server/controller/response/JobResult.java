package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.entity.JobResultNode;

public record JobResult(
    String id,
    String payload
) implements Serializable {

    public static JobResult fromNodes(JobResultNode resultNode) {
        return new JobResult(
            resultNode.getId(),
            resultNode.payload
        );
    }

}
