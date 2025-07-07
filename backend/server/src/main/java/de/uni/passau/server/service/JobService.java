package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.exception.NamedException;
import de.uni.passau.core.exception.OtherException;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.algorithms.AdjustMaxSets;
import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeFds;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
            LOGGER.warn("Job { id: {}, name: '{}' } is not ready.", job.id(), job.description);
            return;
        }

        job.state = JobState.RUNNING;
        job.startedAt = new java.util.Date();
        job = jobRepository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id(), job.description);

        try {
            executeJobPayload(job);
            job.state = JobState.FINISHED;
            job.finishedAt = new java.util.Date();
            job = jobRepository.save(job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id(), job.description);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);

            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id(), job.description), finalException);
            job.state = JobState.FAILED;
            job.finishedAt = new java.util.Date();
            job.error = finalException.toSerializedException();
            job = jobRepository.save(job);
        }
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

        workflow.state = WorkflowState.NEGATIVE_EXAMPLES;

        final var armstrongRelation = ComputeAR.run(maxSets, dataset, null, false);
        // All example rows are brand new, so we can create assignments for all of them.
        final var assignments = armstrongRelation.exampleRows.stream()
            .map(row -> AssignmentEntity.create(job.workflowId, dataset.getHeader(), armstrongRelation.referenceRow, row))
            .toList();

        storageService.set(workflow.maxSetsId(), maxSets);
        assignmentRepository.saveAll(assignments);
        workflowRepository.save(workflow);

        computeViews(workflow, maxSets, dataset);
    }

    private void executeIterationJob(JobEntity job) {
        final var sb = new StringBuilder();
        sb.append("Iteration job").append(job.workflowId).append("\n\n");

        final var workflow = workflowRepository.findById(job.workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(workflow.datasetId);

        // If there are any evaluated assignments, we have to adjust the max set. This should be the case for all iterations except the first one.
        final var prevMaxSets = storageService.get(workflow.maxSetsId(), MaxSets.class);
        sb.append("Prev: ").append(prevMaxSets).append("\n");

        final var assignments = assignmentRepository.findAllByWorkflowId(job.workflowId);
        final var evaluatedRows = assignments.stream()
            .filter(assignment -> assignment.isActive && assignment.exampleRow.decision != null)
            .map(assignment -> assignment.exampleRow)
            .toList();

        final var adjustedMaxSets = AdjustMaxSets.run(prevMaxSets, evaluatedRows);
        sb.append("Adjusted: ").append(adjustedMaxSets).append("\n");

        // FIXME Check if we can continue the workflow.
        // Also check if we have any assignments left to evaluate.
        // And whether we should switch to positive examples or finish the workflow.
        // If we are done with negative examples, change state to POSITIVE_EXAMPLES.
        // If we are done with positive examples, change state to FINAL.
        workflow.iteration = workflow.iteration + 1;
        final int lhsSize = workflow.iteration;

        // FIXME
        final var isEvaluatingPositives = workflow.state == WorkflowState.POSITIVE_EXAMPLES;

        // Extend the max sets and compute a new Armstrong relation.
        final var extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, lhsSize);
        sb.append("Extended: ").append(extendedMaxSets).append("\n");

        final var prevAR = loadPrevAR(assignments);
        final var armstrongRelation = ComputeAR.run(extendedMaxSets, dataset, prevAR, isEvaluatingPositives);

        // We mark all assignment as inactive. Then we activate all those in the new Armstrong relation.
        assignments.forEach(assignment -> assignment.isActive = false);
        final Map<ColumnSet, AssignmentEntity> assignmentsByLhs = assignments.stream().collect(Collectors.toMap(a -> a.exampleRow.lhsSet, a -> a));

        for (final var exampleRow : armstrongRelation.exampleRows) {
            final var existingAssignment = assignmentsByLhs.get(exampleRow.lhsSet);
            if (existingAssignment != null) {
                // If the assignment already exists, we just update the example row.
                existingAssignment.exampleRow = exampleRow;
                existingAssignment.isActive = true;
                continue;
            }

            // If the assignment doesn't exist, we create a new one.
            final var newAssignment = AssignmentEntity.create(job.workflowId, dataset.getHeader(), armstrongRelation.referenceRow, exampleRow);
            assignments.add(newAssignment);
        }

        storageService.set(workflow.maxSetsId(), extendedMaxSets);
        workflowRepository.save(workflow);
        assignmentRepository.saveAll(assignments);

        computeViews(workflow, extendedMaxSets, dataset);

        LOGGER.info(sb.toString());
    }

    private ArmstrongRelation loadPrevAR(List<AssignmentEntity> assignments) {
        // The list is not sorted, but we don't need it to be.
        final var exampleRows = assignments.stream()
            .filter(assignment -> assignment.isActive)
            .map(assignment -> assignment.exampleRow)
            .toList();

        return new ArmstrongRelation(assignments.get(0).referenceRow, exampleRows);
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

        // TODO Compute lattices
        // for (int classIndex = 0; classIndex < maxSets.sets().size(); classIndex++) {
        //     final var lattice = ComputeLattice.run(maxSets.sets().get(classIndex), initialMaxSets.sets().get(classIndex));
        //     // TODO save all lattices ... or save them by class, so that the use can load them one by one?
        //     storageService.set(workflow.latticesId(), lattice);
        // }

        final var fds = ComputeFds.run(maxSets, dataset.getHeader());
        storageService.set(workflow.fdsId(), fds);
    }

}
