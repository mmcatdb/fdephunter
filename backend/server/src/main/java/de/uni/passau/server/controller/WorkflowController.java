package de.uni.passau.server.controller;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.JobRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.AssignmentService;
import de.uni.passau.server.service.JobService;

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
    private AssignmentService assignmentService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobService jobService;

    @GetMapping("/workflows/{workflowId}")
    public WorkflowEntity getWorkflowById(@PathVariable UUID workflowId) {
        return workflowRepository.findById(workflowId).get();
    }

    @PostMapping("/workflows/create")
    public WorkflowEntity createWorkflow() {
        return workflowRepository.save(WorkflowEntity.create());
    }

    private record CreateJobResponse(
        WorkflowEntity workflow,
        JobEntity job
    ) {
        static CreateJobResponse fromEntities(WorkflowEntity workflow, JobEntity job) {
            return new CreateJobResponse(workflow, job);
        }
    }

    private record StartWorkflowRequest(
        String description,
        ApproachName approach,
        UUID datasetId
    ) {}

    @PostMapping("/workflows/{workflowId}/start")
    public CreateJobResponse startWorkflow(@PathVariable UUID workflowId, @RequestBody StartWorkflowRequest init) {
        var workflow = workflowRepository.findById(workflowId).get();
        workflow.datasetId = init.datasetId;
        workflow.state = WorkflowState.INITIAL_FD_DISCOVERY;
        workflow = workflowRepository.save(workflow);

        final var job = jobService.createDiscoveryJob(workflow, init.description(), init.approach());
        jobService.executeJobAsync(job.id());

        return CreateJobResponse.fromEntities(workflow, job);
    }

    private record ContinueWorkflowRequest(
        String description
    ) {}

    @PostMapping("/workflows/{workflowId}/continue")
    public CreateJobResponse continueWorkflow(@PathVariable UUID workflowId, @RequestBody ContinueWorkflowRequest init) {
        final var workflow = workflowRepository.findById(workflowId).get();

        final var job = jobService.createAdjustJob(workflow, init.description());
        jobService.executeJobAsync(job.id());

        return CreateJobResponse.fromEntities(workflow, job);
    }

    @GetMapping("/workflows/{workflowId}/last-discovery")
    public JobEntity getLastJobByWorkflowId(@PathVariable UUID workflowId) {
        return jobRepository.findLastByWorkflowId(workflowId);
    }

    @PostMapping("/workflows/{workflowId}/accept-all")
    public WorkflowEntity acceptAllAssignments(@PathVariable UUID workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();

        assignmentService.acceptAllAssignments(workflow);

        return workflow;
    }

}
