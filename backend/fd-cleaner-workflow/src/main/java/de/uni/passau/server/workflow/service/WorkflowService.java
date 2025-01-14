/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.service;

import de.uni.passau.server.workflow.model.DiscoveryResultNode;
import de.uni.passau.server.workflow.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.workflow.model.WorkflowNode;
import de.uni.passau.server.workflow.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.workflow.repository.ClassRepository;
import de.uni.passau.server.workflow.repository.ClassRepository.ClassNodeGroup;
import de.uni.passau.server.workflow.repository.DiscoveryResultRepository;
import de.uni.passau.server.workflow.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private DiscoveryResultRepository discoveryResultRepository;

    @Autowired
    private ClassRepository classRepository;

    public Flux<WorkflowNode> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public Mono<Void> removeWorkflow(String id) {
        return workflowRepository.deleteById(id);
    }

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

    public Mono<DiscoveryResultNode> getLastResultByWorkflowId(String workflowId) {
        return discoveryResultRepository.getLastResultByWorkflowId(workflowId);
    }

    public Mono<WorkflowNode> setIteration(String workflowId, int iteration) {
        return workflowRepository.setIteration(workflowId, iteration);
    }

    public Mono<String> getDatasetName(String workflowId) {
        return workflowRepository.getDatasetName(workflowId);
    }

    public Flux<ClassNodeGroup> getClassesForWorkflow(String workflowId) {
        return classRepository.findAllGroupsByWorkflowId(workflowId);
    }

    public Mono<Boolean> canCreateRediscoveryJob(String workflowId) {
        return classRepository.findAllGroupsByWorkflowId(workflowId).collectList().map(classes ->
            classes.stream().allMatch(c -> c.lastExample() != null && c.lastExample().getState() == NegativeExampleState.ACCEPTED)
        );
    }

}
