package de.uni.passau.server.controller.response;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.server.repository.AssignmentRepository.AssignmentNodeGroup;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public record AssignmentResponse(
    String id,
    // ExampleState state,
    int rowIndex,
    String workflowId,
    ExampleRelation relation,
    @Nullable ExampleDecision decision
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

    /** @deprecated */
    public static AssignmentResponse fromNodes(AssignmentNodeGroup group, DatasetData dataset) {
        // return AssignmentResponse.fromNodes(group.assignment(), group.example(), group.result(), dataset);
        // FIXME
        return null;
    }

    /**
     * An example consists of a reference row and an example row. The two rows form a "relation".
     */
    public record ExampleRelation(
        /** Names of the columns. They are expected to be unique. */
        String[] columns,
        /** Values of the reference row. */
        String[] referenceRow,
        /** The example relation contains exactly one example row. */
        ExampleRow exampleRow
    ) {}

}
