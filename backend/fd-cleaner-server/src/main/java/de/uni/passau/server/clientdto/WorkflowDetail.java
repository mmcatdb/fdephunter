package de.uni.passau.server.clientdto;

import de.uni.passau.server.workflow.model.DiscoveryJobNode;
import de.uni.passau.server.workflow.model.WorkflowNode;
import de.uni.passau.server.workflow.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.workflow.repository.ClassRepository.ClassNodeGroup;

import java.io.Serializable;
import java.util.List;

import org.springframework.lang.Nullable;

public record WorkflowDetail(
    String id,
    WorkflowState state,
    int iteration,
    @Nullable List<Class> classes,
    @Nullable DiscoveryJob job
) implements Serializable {

    public static WorkflowDetail fromNodes(WorkflowNode workflowNode, @Nullable List<ClassNodeGroup> classGroups, @Nullable DiscoveryJobNode jobNode) {
        return new WorkflowDetail(
            workflowNode.getId(),
            workflowNode.getState(),
            workflowNode.getIteration(),
            classGroups == null ? null : classGroups.stream().map(Class::fromNodes).toList(),
            jobNode == null ? null : DiscoveryJob.fromNodes(jobNode)
        );
    }

}
