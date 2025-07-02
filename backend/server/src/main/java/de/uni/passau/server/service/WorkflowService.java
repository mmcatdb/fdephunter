package de.uni.passau.server.service;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    public WorkflowNode createWorkflow() {
        return workflowRepository.save(WorkflowNode.createNew());
    }

}
