package de.uni.passau.server.controller;

import de.uni.passau.server.controller.request.DiscoveryJobRequest;
import de.uni.passau.server.controller.request.RediscoveryJobRequest;
import de.uni.passau.server.controller.response.DiscoveryJob;
import de.uni.passau.server.controller.response.Workflow;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
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
    public Mono<Workflow> getWorkflowById(@PathVariable String workflowId) {
        return workflowRepository.findById(workflowId).map(Workflow::fromNodes);
    }

    @PostMapping("/workflows/create")
    public Mono<Workflow> createWorkflow() {
        return workflowService.createWorkflow().map(Workflow::fromNodes);
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
        return discoveryJobService.createDiscoveryJob(workflowId, init.approach(), init.description(), init.dataset())
            .flatMap(jobNode ->
                workflowRepository.findById(workflowId).map(workflowNode -> CreateJobResponse.fromNodes(workflowNode, jobNode))
            );
    }

    @PostMapping("/workflows/{workflowId}/execute-rediscovery")
    public Mono<CreateJobResponse> createRediscoveryJob(@RequestBody RediscoveryJobRequest init, @PathVariable String workflowId) {
        return discoveryJobService.canCreateRediscoveryJob(workflowId).filter(value -> value)
            .switchIfEmpty(Mono.error(new RuntimeException("cannot create new rediscovery job")))
            .then(
                discoveryJobService.createRediscoveryJob(workflowId, init.approach(), init.description())
                    .flatMap(jobNode ->
                        workflowRepository.findById(workflowId).map(workflowNode -> CreateJobResponse.fromNodes(workflowNode, jobNode))
                    )
            );
    }

    @GetMapping("/workflows/{workflowId}/last-discovery")
    public Mono<DiscoveryJob> getLastJobByWorkflowId(@PathVariable String workflowId) {
        return discoveryJobRepository.getLastDiscoveryByWorkflowId(workflowId).map(DiscoveryJob::fromNodes);
    }

    @GetMapping("/workflows/{workflowId}/last-result")
    public Mono<JobResultNode> getLastJobResult(@PathVariable String workflowId) {
        return jobResultRepository.getLastResultByWorkflowId(workflowId);
    }

}
