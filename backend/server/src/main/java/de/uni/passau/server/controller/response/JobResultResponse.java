package de.uni.passau.server.controller.response;

import java.io.Serializable;

import de.uni.passau.server.model.JobResultNode;

public record JobResultResponse(
    String id,
    String payload
) implements Serializable {

    public static JobResultResponse fromNodes(JobResultNode resultNode) {
        return new JobResultResponse(
            resultNode.getId(),
            resultNode.payload
        );
    }

}
