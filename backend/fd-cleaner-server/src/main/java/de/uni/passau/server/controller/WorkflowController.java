/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.controller;

import de.uni.passau.server.clientdto.DiscoveryJob;
import de.uni.passau.server.clientdto.Expert;
import de.uni.passau.server.clientdto.Workflow;
import de.uni.passau.server.clientdto.WorkflowDetail;
import de.uni.passau.server.controller.request.DiscoveryJobRequest;
import de.uni.passau.server.controller.request.RediscoveryJobRequest;
import de.uni.passau.server.workflow.model.DiscoveryJobNode;
import de.uni.passau.server.workflow.model.DiscoveryResultNode;
import de.uni.passau.server.workflow.model.WorkflowNode;
import de.uni.passau.server.workflow.service.DiscoveryJobService;
import de.uni.passau.server.workflow.service.ExpertService;
import de.uni.passau.server.workflow.service.UserService;
import de.uni.passau.server.workflow.service.WorkflowService;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private DiscoveryJobService discoveryJobService;

    @Autowired
    private ExpertService expertService;

    @Autowired
    private UserService userService;

    @Deprecated
    @GetMapping("/workflows")
    public Flux<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows().map(Workflow::fromNodes);
    }

    @GetMapping("/workflows/{workflowId}")
    public Mono<WorkflowDetail> getWorkflowDetailById(@PathVariable String workflowId) {
        return workflowService.getWorkflowById(workflowId).flatMap(workflowNode ->
            discoveryJobService.getLastDiscoveryByWorkflowId(workflowId).map(Optional::of).switchIfEmpty(Mono.just(Optional.empty()))
                .flatMap(optionalJobNode ->
                    workflowService.getClassesForWorkflow(workflowId).collectList().map(classGroups ->
                        WorkflowDetail.fromNodes(workflowNode, classGroups, optionalJobNode.orElse(null))
                    )
                )
        );
    }

    @PostMapping("/workflows/create")
    public Mono<Workflow> createWorkflow() {
        return workflowService.createWorkflow().map(Workflow::fromNodes);
    }

    // @GetMapping("/workflows/{workflowId}/set-owner/{userId}")
    // public Mono<UserNode> setOwnerOfWorkflow(@PathVariable String workflowId, @PathVariable String userId) {
    //     return userService.makeOwnerOfWorkflow(userId, workflowId);
    // }

    @GetMapping("/workflows/{workflowId}/experts")
    public Flux<Expert> getAllExpertsInWorkflow(@PathVariable String workflowId) {
        return expertService.findAllInWorkflow(workflowId).map(Expert::fromNodes);
    }

    private record AddExpertInput(
        String userId
    ) {}

    @PostMapping("/workflows/{workflowId}/add-expert")
    public Mono<Expert> addExpertToWorkflow(@PathVariable String workflowId, @RequestBody AddExpertInput input) {
        return userService.makeExpertOfWorkflow(input.userId, workflowId).map(Expert::fromNodes);
    }

    private record CreateJobResponse(
        Workflow workflow,
        DiscoveryJob job
    ) {
        static CreateJobResponse fromNodes(WorkflowNode workflowNode, DiscoveryJobNode jobNode) {
            return new CreateJobResponse(Workflow.fromNodes(workflowNode), DiscoveryJob.fromNodes(jobNode));
        }
    }

    @PostMapping("/workflows/{workflowId}/execute-discovery")
    public Mono<CreateJobResponse> createDiscoveryJob(@RequestBody DiscoveryJobRequest init, @PathVariable String workflowId) {
        return discoveryJobService.createDiscoveryJob(workflowId, init.getApproach(), init.getDescription(), init.getDatasets())
            .flatMap(jobNode ->
                workflowService.getWorkflowById(workflowId).map(workflowNode -> CreateJobResponse.fromNodes(workflowNode, jobNode))
            );
    }

    @PostMapping("/workflows/{workflowId}/execute-rediscovery")
    public Mono<CreateJobResponse> createRediscoveryJob(@RequestBody RediscoveryJobRequest init, @PathVariable String workflowId) {
        return discoveryJobService.canCreateRediscoveryJob(workflowId).filter(value -> value)
            .switchIfEmpty(Mono.error(new RuntimeException("cannot create new rediscovery job")))
            .then(
                discoveryJobService.createRediscoveryJob(workflowId, init.getApproach(), init.getDescription())
                    .flatMap(jobNode ->
                        workflowService.getWorkflowById(workflowId).map(workflowNode -> CreateJobResponse.fromNodes(workflowNode, jobNode))
                    )
            );
    }

    @Deprecated
    @GetMapping("/jobs")
    public Flux<DiscoveryJobNode> getAllJobs() {
        return discoveryJobService.getAllJobs();
    }

    @GetMapping("/workflows/{workflowId}/last-discovery")
    public Mono<DiscoveryJob> getLastJobByWorkflowId(@PathVariable String workflowId) {
        // informace o jobu, nevracej vysledky (uzivatel musi potvrdit)
        return discoveryJobService.getLastDiscoveryByWorkflowId(workflowId).map(DiscoveryJob::fromNodes);
    }

    @GetMapping("/workflows/{workflowId}/last-result")
    public Mono<DiscoveryResultNode> getLastDiscoveryResult(@PathVariable String workflowId) {
        return workflowService.getLastResultByWorkflowId(workflowId);
    }

    @GetMapping("/workflows/{workflowId}/classes")
    public Flux<de.uni.passau.server.clientdto.Class> getClasses(@PathVariable String workflowId) {
        return workflowService.getClassesForWorkflow(workflowId).map(de.uni.passau.server.clientdto.Class::fromNodes);
    }

}
