package de.uni.passau.server.controller.response;

import de.uni.passau.core.example.ExampleRow;

import java.io.Serializable;
import java.util.UUID;

public record AssignmentResponse(
    String id,
    UUID workflowId,
    ExampleRelation relation
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
