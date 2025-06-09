package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.AssignmentNode.AssignmentVerdict;
import de.uni.passau.server.model.DiscoveryResultNode;
import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.repository.AssignmentRepository.AssignmentNodeGroup;

import java.io.Serializable;

public record Assignment(
    String id,
    AssignmentVerdict verdict,
    NegativeExample example,
    DiscoveryResult discoveryResult,
    DatasetData dataset
) implements Serializable {

    public static Assignment fromNodes(AssignmentNode assignmentNode, NegativeExampleNode exampleNode, DiscoveryResultNode resultNode, DatasetData dataset) {
        return new Assignment(
            assignmentNode.getId(),
            assignmentNode.getVerdict(),
            NegativeExample.fromNodes(exampleNode),
            DiscoveryResult.fromNodes(resultNode),
            dataset
        );
    }

    public static Assignment fromNodes(AssignmentNodeGroup group, DatasetData dataset) {
        return Assignment.fromNodes(group.assignment(), group.example(), group.result(), dataset);
    }

}
