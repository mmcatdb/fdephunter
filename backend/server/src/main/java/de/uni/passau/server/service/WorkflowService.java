package de.uni.passau.server.service;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ClassRepository classRepository;

    public WorkflowNode createWorkflow() {
        return workflowRepository.save(WorkflowNode.createNew());
    }

    public boolean canCreateRediscoveryJob(String workflowId) {
        return classRepository.findAllGroupsByWorkflowId(workflowId).stream()
            .allMatch(c -> c.lastExample() != null && c.lastExample().state == NegativeExampleState.ACCEPTED);
    }

}
