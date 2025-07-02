package de.uni.passau.server.controller;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.controller.response.DiscoveryJobResponse;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.DiscoveryJobService;
import de.uni.passau.server.service.WorkflowService;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;

    @Autowired
    private DiscoveryJobService discoveryJobService;

    @GetMapping("/workflows/{workflowId}")
    public WorkflowEntity getWorkflowById(@PathVariable UUID workflowId) {
        return workflowRepository.findById(workflowId).get();
    }

    @PostMapping("/workflows/create")
    public WorkflowEntity createWorkflow() {
        return workflowService.createWorkflow();
    }

    private record CreateJobResponse(
        WorkflowEntity workflow,
        DiscoveryJobResponse job
    ) {
        static CreateJobResponse fromNodes(WorkflowEntity workflow, DiscoveryJobNode jobNode) {
            return new CreateJobResponse(workflow, DiscoveryJobResponse.fromNodes(jobNode));
        }
    }

    private record StartWorkflowRequest(
        String description,
        ApproachName approach,
        UUID datasetId
    ) {}

    @PostMapping("/workflows/{workflowId}/start")
    public CreateJobResponse startWorkflow(@RequestBody StartWorkflowRequest init, @PathVariable UUID workflowId) {
        final var job = discoveryJobService.createDiscoveryJob(workflowId, init.approach(), init.description(), init.datasetId());
        final var workflow = workflowRepository.findById(workflowId).get();

        return CreateJobResponse.fromNodes(workflow, job);
    }

    private record ContinueWorkflowRequest(
        String description
    ) {}

    @PostMapping("/workflows/{workflowId}/continue")
    public CreateJobResponse continueWorkflow(@RequestBody ContinueWorkflowRequest init, @PathVariable UUID workflowId) {
        // final var job = discoveryJobService.createDiscoveryJob(workflowId, init.approach(), init.description(), init.dataset());
        // final var workflow = workflowRepository.findById(workflowId).get();

        // return CreateJobResponse.fromNodes(workflow, job);

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping("/workflows/{workflowId}/last-discovery")
    public DiscoveryJobResponse getLastJobByWorkflowId(@PathVariable UUID workflowId) {
        final var job = discoveryJobRepository.getLastDiscoveryByWorkflowId(workflowId);
        return DiscoveryJobResponse.fromNodes(job);
    }

}
