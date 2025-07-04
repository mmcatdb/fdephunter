package de.uni.passau.server.controller;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.JobRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.AssignmentService;
import de.uni.passau.server.service.JobService;
import de.uni.passau.server.service.StorageService;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private StorageService storageService;

    @GetMapping("/workflows/{workflowId}")
    public WorkflowEntity getWorkflowById(@PathVariable UUID workflowId) {
        return workflowRepository.findById(workflowId).get();
    }

    @PostMapping("/workflows/create")
    public WorkflowEntity createWorkflow() {
        // TODO The object can't be a list.

        final var workflow = WorkflowEntity.create();

        final var list = new ArrayList<ColumnSet>();

        list.add(ColumnSet.fromIndexes(0, 1, 3, 77));
        list.add(ColumnSet.fromIndexes(5, 6, 7));
        list.add(ColumnSet.fromIndexes(2, 4, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21));

        final var sets = new MaxSets(list, null);

        storageService.set(workflow.arId(), sets);

        return workflowRepository.save(workflow);
    }

    private record MaxSets(
        List<ColumnSet> sets,
        List<ColumnSet> candidates
    ) {}

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

        final var job = jobService.createIterationJob(workflow, init.description());
        jobService.executeJobAsync(job.id());

        return CreateJobResponse.fromEntities(workflow, job);
    }

    @GetMapping("/workflows/{workflowId}/last-job")
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
