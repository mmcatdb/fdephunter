package de.uni.passau.server.controller;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.controller.response.DiscoveryJobResponse;
import de.uni.passau.server.controller.response.WorkflowResponse;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.JobResultNode;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.DiscoveryJobService;
import de.uni.passau.server.service.WorkflowService;

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
    private JobResultRepository jobResultRepository;

    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;

    @Autowired
    private DiscoveryJobService discoveryJobService;

    @GetMapping("/workflows/{workflowId}")
    public WorkflowResponse getWorkflowById(@PathVariable String workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();
        return WorkflowResponse.fromNodes(workflow);
    }

    @PostMapping("/workflows/create")
    public WorkflowResponse createWorkflow() {
        final var workflow = workflowService.createWorkflow();
        return WorkflowResponse.fromNodes(workflow);
    }

    private record CreateJobResponse(
        WorkflowResponse workflow,
        DiscoveryJobResponse job
    ) {
        static CreateJobResponse fromNodes(WorkflowNode workflowNode, DiscoveryJobNode jobNode) {
            return new CreateJobResponse(WorkflowResponse.fromNodes(workflowNode), DiscoveryJobResponse.fromNodes(jobNode));
        }
    }

    private record StartWorkflowRequest(
        String description,
        ApproachName approach,
        String dataset
    ) {}

    @PostMapping("/workflows/{workflowId}/start")
    public CreateJobResponse startWorkflow(@RequestBody StartWorkflowRequest init, @PathVariable String workflowId) {
        final var job = discoveryJobService.createDiscoveryJob(workflowId, init.approach(), init.description(), init.dataset());
        final var workflow = workflowRepository.findById(workflowId).get();

        return CreateJobResponse.fromNodes(workflow, job);
    }

    private record ContinueWorkflowRequest(
        String description
    ) {}

    @PostMapping("/workflows/{workflowId}/continue")
    public CreateJobResponse continueWorkflow(@RequestBody ContinueWorkflowRequest init, @PathVariable String workflowId) {
        final var job = discoveryJobService.createDiscoveryJob(workflowId, init.approach(), init.description(), init.dataset());
        final var workflow = workflowRepository.findById(workflowId).get();

        return CreateJobResponse.fromNodes(workflow, job);
    }

    @GetMapping("/workflows/{workflowId}/last-discovery")
    public DiscoveryJobResponse getLastJobByWorkflowId(@PathVariable String workflowId) {
        final var job = discoveryJobRepository.getLastDiscoveryByWorkflowId(workflowId);
        return DiscoveryJobResponse.fromNodes(job);
    }

    @GetMapping("/workflows/{workflowId}/last-result")
    public JobResultNode getLastJobResult(@PathVariable String workflowId) {
        return jobResultRepository.getLastResultByWorkflowId(workflowId);
    }

}
