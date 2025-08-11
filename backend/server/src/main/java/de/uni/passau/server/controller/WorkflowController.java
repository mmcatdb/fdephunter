package de.uni.passau.server.controller;

import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.DatasetRepository;
import de.uni.passau.server.repository.JobRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.AssignmentService;
import de.uni.passau.server.service.DatasetService;
import de.uni.passau.server.service.JobService;
import de.uni.passau.server.service.DatasetService.CsvDatasetInit;

import java.io.Serializable;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
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
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetService datasetService;

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
    ) implements Serializable {
        static CreateJobResponse fromEntities(WorkflowEntity workflow, JobEntity job) {
            return new CreateJobResponse(workflow, job);
        }
    }

    private record StartWorkflowRequest(
        @Nullable UUID datasetId,
        @Nullable CsvDatasetInit datasetInit
    ) implements Serializable {}

    @PostMapping("/workflows/{workflowId}/start")
    public CreateJobResponse startWorkflow(@PathVariable UUID workflowId, @RequestBody StartWorkflowRequest request) {
        if (request.datasetId == null && request.datasetInit == null)
            throw new IllegalArgumentException("Either datasetId or file must be provided.");

        final var dataset = request.datasetId != null
            ? datasetRepository.findById(request.datasetId).get()
            : datasetService.createDataset(request.datasetInit);

        var workflow = workflowRepository.findById(workflowId).get();
        workflow.datasetId = dataset.id();
        workflow.state = WorkflowState.INITIAL_FD_DISCOVERY;
        workflow = workflowRepository.save(workflow);

        final String description = "Initial discovery for " + dataset.name;

        final var job = jobService.createDiscoveryJob(workflow, description);
        jobService.executeJobAsync(job.id());

        return CreateJobResponse.fromEntities(workflow, job);
    }

    @PostMapping("/workflows/{workflowId}/continue")
    public CreateJobResponse continueWorkflow(@PathVariable UUID workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();

        final String description = (workflow.state == WorkflowState.NEGATIVE_EXAMPLES && workflow.lhsSize == 0)
            ? "Wait for negative example generation ..."
            : "Applying approved examples ...";

        final var job = jobService.createEvaluationJob(workflow, description);
        jobService.executeJobAsync(job.id());

        return CreateJobResponse.fromEntities(workflow, job);
    }

    @GetMapping("/workflows/{workflowId}/last-job")
    public @Nullable JobEntity getLastJobByWorkflowId(@PathVariable UUID workflowId) {
        return jobRepository.findFirstByWorkflowId(workflowId);
    }

    @PostMapping("/workflows/{workflowId}/accept-all")
    public WorkflowEntity acceptAllAssignments(@PathVariable UUID workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();

        assignmentService.acceptAllAssignments(workflow);

        return workflow;
    }

}
