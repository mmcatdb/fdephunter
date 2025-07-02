package de.uni.passau.server.service;

import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    public WorkflowEntity createWorkflow() {
        return workflowRepository.save(WorkflowEntity.create());
    }

}
