package de.uni.passau.server.controller.response;

import de.uni.passau.server.model.AssignmentDecision;
import de.uni.passau.server.model.ExampleRelation;
import de.uni.passau.server.model.entity.AssignmentNode;
import de.uni.passau.server.model.entity.AssignmentNode.AssignmentState;
import de.uni.passau.server.repository.AssignmentRepository.AssignmentNodeGroup;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public record AssignmentResponse(
    String id,
    AssignmentState state,
    int rowIndex,
    String workflowId,
    ExampleRelation relation,
    @Nullable AssignmentDecision decision
) implements Serializable {

    // public static AssignmentResponse fromNodes(AssignmentNode assignmentNode, NegativeExampleNode exampleNode, JobResultNode resultNode, DatasetData dataset) {
    //     return new AssignmentResponse(
    //         assignmentNode.getId(),
    //         assignmentNode.state,
    //         NegativeExample.fromNodes(exampleNode),
    //         JobResult.fromNodes(resultNode),
    //         dataset
    //     );
    // }

    // public static AssignmentResponse fromNodes(AssignmentNodeGroup group, DatasetData dataset) {
    //     return AssignmentResponse.fromNodes(group.assignment(), group.example(), group.result(), dataset);
    // }

}
