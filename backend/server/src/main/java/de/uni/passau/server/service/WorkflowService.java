package de.uni.passau.server.service;

import de.uni.passau.server.model.JobResultNode;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private JobResultRepository jobResultRepository;

    @Autowired
    private ClassRepository classRepository;

    public Mono<WorkflowNode> createWorkflow() {
        return workflowRepository.save(WorkflowNode.createNew());
    }

    public Mono<WorkflowNode> saveWorkflow(WorkflowNode node) {
        return workflowRepository.save(node);
    }

    public Mono<WorkflowNode> getWorkflowById(String workflowId) {
        return workflowRepository.findById(workflowId);
    }

    public Mono<Void> purge() {
        return workflowRepository.purgeDatabase();
    }

    public Mono<WorkflowNode> setState(String workflowId, WorkflowState state) {
        return workflowRepository.setState(workflowId, state);
    }

    public Mono<JobResultNode> getLastResultByWorkflowId(String workflowId) {
        return jobResultRepository.getLastResultByWorkflowId(workflowId);
    }

    public Mono<WorkflowNode> setIteration(String workflowId, int iteration) {
        return workflowRepository.setIteration(workflowId, iteration);
    }

    public Mono<String> getDatasetName(String workflowId) {
        return workflowRepository.getDatasetName(workflowId);
    }

    public Mono<Boolean> canCreateRediscoveryJob(String workflowId) {
        return classRepository.findAllGroupsByWorkflowId(workflowId).collectList().map(classes ->
            classes.stream().allMatch(c -> c.lastExample() != null && c.lastExample().state == NegativeExampleState.ACCEPTED)
        );
    }

}
