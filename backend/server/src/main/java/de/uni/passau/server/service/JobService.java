package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeMaxSet;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.AdjustJobPayload;
import de.uni.passau.server.model.JobEntity.DiscoveryJobPayload;
import de.uni.passau.server.model.JobEntity.JobState;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.JobRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobResultRepository jobResultRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TaskExecutor taskExecutor;

    public JobEntity createDiscoveryJob(WorkflowEntity workflow, String description, ApproachName approachName) {
        // This is supposed to be the first job in the workflow, so index is 0.
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.getId());
        if (prevJobsCount != 0)
            throw new IllegalStateException("Cannot create a discovery job when there are already jobs in the workflow.");

        final var payload = new DiscoveryJobPayload(workflow.datasetId, approachName);
        final var job = JobEntity.create(workflow.getId(), 0, description, payload);
        return jobRepository.save(job);
    }

    public JobEntity createAdjustJob(WorkflowEntity workflow, String description) {
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.getId());
        final var payload = new AdjustJobPayload();
        final var job = JobEntity.create(workflow.getId(), prevJobsCount, description, payload);
        return jobRepository.save(job);
    }

    public void executeJobAsync(UUID jobId) {
        final var jobTask = new JobTask(jobId);
        taskExecutor.execute(jobTask);
    }

    private class JobTask implements Runnable {

        private final UUID jobId;

        public JobTask(UUID jobId) {
            this.jobId = jobId;
        }

        @Override
        public void run() {
            executeJob(jobId);
        }
    }

    private void executeJob(UUID jobId) {
        var job = jobRepository.findById(jobId).get();
        if (job.state != JobState.WAITING) {
            LOGGER.warn("Job {} is not in WAITING state, skipping execution.", jobId);
            return;
        }

        job.state = JobState.RUNNING;
        job.startedAt = new java.util.Date();
        job = jobRepository.save(job);
        LOGGER.info("Job {} has started at {}.", job.getId(), job.startedAt);

        try {
            executeJobPayload(job);
        }
        catch (Exception e) {
            LOGGER.error("Error while executing job {}: {}", job.getId(), e.getMessage(), e);
            job.state = JobState.FAILED;
            job.finishedAt = new java.util.Date();
            job = jobRepository.save(job);
            LOGGER.info("Job {} has failed at {}.", job.getId(), job.finishedAt);
            return;
        }

        job.state = JobState.FINISHED;
        job.finishedAt = new java.util.Date();
        job = jobRepository.save(job);
        LOGGER.info("Job {} has finished at {}.", job.getId(), job.finishedAt);
    }

    private void executeJobPayload(JobEntity job) {
        // TODO Change this to switch once we are on a better java version.
        if (job.payload instanceof DiscoveryJobPayload)
            executeDiscoveryJob(job);
        else if (job.payload instanceof AdjustJobPayload)
            executeAdjustJob(job);
    }

    private void executeDiscoveryJob(JobEntity job) {
        final var payload = (DiscoveryJobPayload) job.payload;
        final var workflow = workflowRepository.findById(job.workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(payload.datasetId());

        final var maxSets = ComputeMaxSet.run(dataset);

        // TODO Save max set.

        // TODO compute lattice

        // TODO compute fds

        final var armstrongRelation = ComputeAR.run(maxSets, dataset);

        final var assignments = new ArrayList<AssignmentEntity>();

        // TODO This should be the minimal size of columnSets in the examples.
        final int columnSetSize = 1;

        for (final var exampleRow : armstrongRelation.exampleRows) {
            // We don't need to create examples for each row.
            // TODO Optimize this in the algorithm itself.
            if (exampleRow.maxSetElement.size() != columnSetSize)
                continue;

            final var assignment = AssignmentEntity.create(job.workflowId, armstrongRelation.columns, armstrongRelation.referenceRow, exampleRow);
            assignments.add(assignment);
        }

        // TODO Save the whole relation, not just the assignments.

        assignmentRepository.saveAll(assignments);

        workflow.iteration = workflow.iteration + 1;
        workflow.state = WorkflowState.NEGATIVE_EXAMPLES;
        workflowRepository.save(workflow);
    }

    private void executeAdjustJob(JobEntity job) {
        // TODO Implement the logic for adjusting the max set and generating examples.
        // This is a placeholder for the actual implementation.
        LOGGER.info("Executing adjust job with ID: {}", job.getId());

        // If we are done with negative examples, change state to POSITIVE_EXAMPLES.
        // If we are done with positive examples, change state to FINAL.
    }

}
