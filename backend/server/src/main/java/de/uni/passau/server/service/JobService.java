package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.algorithms.AdjustMaxSets;
import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeFdSet;
import de.uni.passau.algorithms.ComputeLattice;
import de.uni.passau.algorithms.ComputeMaxSets;
import de.uni.passau.algorithms.ExtendMaxSets;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.IterationJobPayload;
import de.uni.passau.server.model.JobEntity.DiscoveryJobPayload;
import de.uni.passau.server.model.JobEntity.JobState;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.JobRepository;
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
    private WorkflowRepository workflowRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TaskExecutor asyncExecutor;

    public JobEntity createDiscoveryJob(WorkflowEntity workflow, String description) {
        // This is supposed to be the first job in the workflow, so index is 0.
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.id());
        if (prevJobsCount != 0)
            throw new IllegalStateException("Cannot create a discovery job when there are already jobs in the workflow.");

        final var payload = new DiscoveryJobPayload(workflow.datasetId);
        final var job = JobEntity.create(workflow.id(), 0, description, payload);
        return jobRepository.save(job);
    }

    public JobEntity createIterationJob(WorkflowEntity workflow, String description) {
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.id());
        final var payload = new IterationJobPayload();
        final var job = JobEntity.create(workflow.id(), prevJobsCount, description, payload);
        return jobRepository.save(job);
    }

    public void executeJobAsync(UUID jobId) {
        final var jobTask = new JobTask(jobId);
        asyncExecutor.execute(jobTask);
    }

    private class JobTask implements Runnable {

        private final UUID jobId;

        public JobTask(UUID jobId) {
            this.jobId = jobId;
        }

        @Override public void run() {
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
        LOGGER.info("Job {} has started at {}.", job.id(), job.startedAt);

        try {
            executeJobPayload(job);
        }
        catch (Exception e) {
            LOGGER.error("Error while executing job {}: {}", job.id(), e.getMessage(), e);
            job.state = JobState.FAILED;
            job.finishedAt = new java.util.Date();
            job = jobRepository.save(job);
            LOGGER.info("Job {} has failed at {}.", job.id(), job.finishedAt);
            return;
        }

        job.state = JobState.FINISHED;
        job.finishedAt = new java.util.Date();
        job = jobRepository.save(job);
        LOGGER.info("Job {} has finished at {}.", job.id(), job.finishedAt);
    }

    private void executeJobPayload(JobEntity job) {
        // NICE_TO_HAVE Change this to switch once we are on a better java version.
        if (job.payload instanceof DiscoveryJobPayload)
            executeDiscoveryJob(job);
        else if (job.payload instanceof IterationJobPayload)
            executeIterationJob(job);
    }

    private void executeDiscoveryJob(JobEntity job) {
        final var payload = (DiscoveryJobPayload) job.payload;
        final var workflow = workflowRepository.findById(job.workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(payload.datasetId());

        final var maxSets = ComputeMaxSets.run(dataset);
        storageService.set(workflow.maxSetsId(), maxSets);

        workflow.state = WorkflowState.NEGATIVE_EXAMPLES;

        final var armstrongRelation = ComputeAR.run(maxSets, dataset, 0);

        final var assignments = new ArrayList<AssignmentEntity>();

        // TODO These are not assignments, just the example rows.
        for (final var exampleRow : armstrongRelation.exampleRows) {
            final var assignment = AssignmentEntity.create(job.workflowId, armstrongRelation.columns, armstrongRelation.referenceRow, exampleRow);
            assignments.add(assignment);
        }

        // TODO Save the whole relation, not just the assignments.

        assignmentRepository.saveAll(assignments);

        workflowRepository.save(workflow);

        computeViews(workflow, maxSets, dataset);
    }

    private void executeIterationJob(JobEntity job) {
        final var workflow = workflowRepository.findById(job.workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(workflow.datasetId);

        // If there are any evaluated assignments, we have to adjust the max set. This should be the case for all iterations except the first one.
        final var maxSets = storageService.get(workflow.maxSetsId(), MaxSets.class);

        final var assignments = assignmentRepository.findAllByWorkflowId(job.workflowId);

        final var evaluatedRows = assignments.stream()
        // TODO Filter only the new ones.
            .filter(assignment -> assignment.exampleRow.decision != null)
            .map(assignment -> assignment.exampleRow)
            .toList();

        final var adjustedMaxSets = AdjustMaxSets.run(maxSets, evaluatedRows);

        // TODO Check if we can continue the workflow.
        // Also check if we have any assignments left to evaluate.
        // And whether we should switch to positive examples or finish the workflow.
        // If we are done with negative examples, change state to POSITIVE_EXAMPLES.
        // If we are done with positive examples, change state to FINAL.
        workflow.iteration = workflow.iteration + 1;
        final int lhsSize = workflow.iteration;

        final var extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, lhsSize);
        storageService.set(workflow.maxSetsId(), extendedMaxSets);

        final var armstrongRelation = ComputeAR.run(maxSets, dataset, lhsSize);

        final var newAssigmnets = new ArrayList<AssignmentEntity>();
        for (final var exampleRow : armstrongRelation.exampleRows) {
            // We don't need to create examples for each row.
            // TODO Optimize this in the algorithm itself.
            if (exampleRow.lhsSet.size() != lhsSize)
                continue;

            final var assignment = AssignmentEntity.create(job.workflowId, armstrongRelation.columns, armstrongRelation.referenceRow, exampleRow);
            newAssigmnets.add(assignment);
        }


        // TODO compute lattice

        // TODO compute fds


        workflowRepository.save(workflow);

        computeViews(workflow, maxSets, dataset);
    }

    private void computeViews(WorkflowEntity workflow, MaxSets maxSets, Dataset dataset) {
        MaxSets initialMaxSets;
        if (workflow.iteration == 0) {
            initialMaxSets = maxSets;
            storageService.set(workflow.initialMaxSetsId(), initialMaxSets);
        }
        else {
            initialMaxSets = storageService.get(workflow.initialMaxSetsId(), MaxSets.class);
        }

        // TODO
        // for (int classIndex = 0; classIndex < maxSets.sets().size(); classIndex++) {
        //     final var lattice = ComputeLattice.run(maxSets.sets().get(classIndex), initialMaxSets.sets().get(classIndex));
        //     // TODO save all lattices ... or save them by class, so that the use can load them one by one?
        //     storageService.set(workflow.latticesId(), lattice);
        // }

        final var fds = ComputeFdSet.run(maxSets, dataset.getHeader());
        storageService.set(workflow.fdsId(), fds);
    }

}
