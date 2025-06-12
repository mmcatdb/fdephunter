package de.uni.passau.server.service;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ClassRepository classRepository;

    public Mono<WorkflowNode> createWorkflow() {
        return workflowRepository.save(WorkflowNode.createNew());
    }

    public Mono<Boolean> canCreateRediscoveryJob(String workflowId) {
        return classRepository.findAllGroupsByWorkflowId(workflowId).collectList().map(classes ->
            classes.stream().allMatch(c -> c.lastExample() != null && c.lastExample().state == NegativeExampleState.ACCEPTED)
        );
    }

}
