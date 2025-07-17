package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.exception.NamedException;
import de.uni.passau.core.exception.OtherException;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.algorithms.AdjustMaxSets;
import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeFds;
import de.uni.passau.algorithms.ComputeLattice;
import de.uni.passau.algorithms.ComputeMaxSets;
import de.uni.passau.algorithms.ExtendMaxSets;
import de.uni.passau.server.exception.JobException;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.EvaluationJobPayload;
import de.uni.passau.server.model.JobEntity.DiscoveryJobPayload;
import de.uni.passau.server.model.JobEntity.JobState;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.JobRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import java.util.ArrayList;
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

    public JobEntity createEvaluationJob(WorkflowEntity workflow, String description) {
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.id());
        final var payload = new EvaluationJobPayload();
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
        else if (job.payload instanceof EvaluationJobPayload)
            executeEvaluationJob(job);
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

    // TODO Create a new service, instantiated per-request, just for this.

    private void executeEvaluationJob(JobEntity job) {
        final var sb = new StringBuilder();
        sb.append("Evaluation job").append(job.workflowId).append("\n\n");

        final var workflow = workflowRepository.findById(job.workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(workflow.datasetId);

        // If there are any evaluated assignments, we have to adjust the max set. This should be the case for all iterations except the first one.
        final var prevMaxSets = storageService.get(workflow.maxSetsId(), MaxSets.class);
        sb.append("Prev: ").append(prevMaxSets).append("\n");

        final var assignments = assignmentRepository.findAllByWorkflowId(job.workflowId);
        final var evaluatedRows = loadEvaluatedRowsAndUpdateAssignments(assignments);

        final var adjustedMaxSets = AdjustMaxSets.run(prevMaxSets, evaluatedRows);
        sb.append("Adjusted: ").append(adjustedMaxSets).append("\n");

        // Now we have adjusted the previous max sets, i.e., we have removed candidates with size = lhsSize.
        // TODO if positives, we should subtract 1 ...
        workflow.lhsSize = workflow.lhsSize + 1;
        if (workflow.lhsSize == adjustedMaxSets.sets().size()) {
            // We have processed the largest possible lattice elements. Let's continue with positive examples.
            workflow.state = WorkflowState.POSITIVE_EXAMPLES;
            workflow.lhsSize = findLhsSizeOfPositiveExamples(workflow, adjustedMaxSets);

            if (workflow.lhsSize == 0) {
                // If there are no positive examples, we can finish the workflow.
                workflow.state = WorkflowState.FINAL;

                // TODO end here!
            }
        }

        // Extend the max sets and compute a new Armstrong relation.
        final var extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, workflow.lhsSize);
        sb.append("Extended: ").append(extendedMaxSets).append("\n");

        final var isAllFinished = extendedMaxSets.sets().stream().allMatch(MaxSet::isFinished);
        if (isAllFinished) {
            // The algorithm finished prematurely (giggity).

            // TODO Try positive examples.
            // If already there (or nothing to do), finish the workflow.
        }

        final var prevAR = loadPrevAR(assignments);
        final var armstrongRelation = ComputeAR.run(extendedMaxSets, dataset, prevAR, workflow.state == WorkflowState.POSITIVE_EXAMPLES);

        addNewAssignments(assignments, armstrongRelation, job, dataset);

        storageService.set(workflow.maxSetsId(), extendedMaxSets);
        workflowRepository.save(workflow);
        assignmentRepository.saveAll(assignments);

        computeViews(workflow, extendedMaxSets, dataset);

        LOGGER.info(sb.toString());
    }

    private List<ExampleRow> loadEvaluatedRowsAndUpdateAssignments(List<AssignmentEntity> assignments) {
        final List<ExampleRow> output = new ArrayList<>();

        for (final var assignment : assignments) {
            if (!assignment.isActive)
                // Only active assignments are considered - others were already processed.
                continue;
            assignment.isActive = false;

            if (assignment.exampleRow.decision == null)
                // All assignments must be decided before we can continue with the workflow.
                throw JobException.assignmentUndecided(assignment.id());

            output.add(assignment.exampleRow);
        }

        return output;
    }

    /**
     * Returns the size of the largest element from max set that is a positive example.
     * If there are none, returns 0.
     */
    private int findLhsSizeOfPositiveExamples(WorkflowEntity workflow, MaxSets currentSets) {
        final var initialSets = storageService.get(workflow.initialMaxSetsId(), MaxSets.class);

        int lhsSize = 0;

        for (int i = 0; i < currentSets.sets().size(); i++) {
            final var initialSet = initialSets.sets().get(i);
            final var currentSet = currentSets.sets().get(i);

            for (final var initial : initialSet.confirmedElements()) {
                if (currentSet.hasConfirmed(initial)) {
                    // We found an initial set element that is still in the current set. That's the definition of a positive example!
                    lhsSize = Math.max(lhsSize, initial.size());
                }
            }
        }

        return lhsSize;
    }

    private ArmstrongRelation loadPrevAR(List<AssignmentEntity> assignments) {
        // The list is not sorted, but we don't need it to be.
        final var exampleRows = assignments.stream()
            .filter(assignment -> assignment.isActive)
            .map(assignment -> assignment.exampleRow)
            .toList();

        return new ArmstrongRelation(assignments.get(0).referenceRow, exampleRows);
    }

    private void addNewAssignments(List<AssignmentEntity> assignments, ArmstrongRelation armstrongRelation, JobEntity job, Dataset dataset) {
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
    }

    private void computeViews(WorkflowEntity workflow, MaxSets maxSets, Dataset dataset) {
        MaxSets initialMaxSets;
        if (workflow.lhsSize == 0) {
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
