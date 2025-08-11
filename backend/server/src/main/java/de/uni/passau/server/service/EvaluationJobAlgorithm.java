package de.uni.passau.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.uni.passau.algorithms.AdjustMaxSets;
import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeFds;
import de.uni.passau.algorithms.ContractMaxSets;
import de.uni.passau.algorithms.ExtendMaxSets;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.server.exception.JobException;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.WorkflowRepository;

@Component
@Scope("prototype")
public class EvaluationJobAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationJobAlgorithm.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StorageService storageService;

    private JobEntity job;
    private WorkflowEntity workflow;
    private Dataset dataset;
    private List<AssignmentEntity> assignments;
    private MaxSets initialMaxSets;

    private MaxSets maxSets;

    public void execute(JobEntity job) {
        LOGGER.debug("Evaluation job {} for workflow {}", job.id(), job.workflowId);

        this.job = job;
        workflow = workflowRepository.findById(job.workflowId).get();
        if (workflow.state == WorkflowState.FINAL)
            // This should not happen, but just in case.
            return;
        dataset = datasetService.getLoadedDatasetById(workflow.datasetId);
        assignments = assignmentRepository.findAllByWorkflowId(job.workflowId);
        // They are going to be needed later, one way or another.
        initialMaxSets = storageService.get(workflow.initialMaxSetsId(), MaxSets.class);

        adjustMaxSets();

        extendMaxSets();

        // This part is valid even if the workflow is finished. We might still need to remove some previous assignments.
        final var prevAR = loadPrevAR();
        final var nextAR = ComputeAR.run(maxSets, dataset, prevAR, workflow.state == WorkflowState.POSITIVE_EXAMPLES);

        addNewAssignments(nextAR);

        storageService.set(workflow.maxSetsId(), maxSets);
        workflowRepository.save(workflow);
        assignmentRepository.saveAll(assignments);

        computeViews();
    }

    private void adjustMaxSets() {
        // If there are any evaluated assignments, we have to adjust the max set.
        // This should be the case for all iterations except the first one (for the first one, the algorithm just does nothing).
        final var prevMaxSets = storageService.get(workflow.maxSetsId(), MaxSets.class);
        LOGGER.debug("Previous max sets:\n{}", prevMaxSets);

        final var isEvaluatingPositives = workflow.state == WorkflowState.POSITIVE_EXAMPLES;
        final var evaluatedRows = new ArrayList<ExampleRow>();

        for (final var assignment : assignments) {
            if (!assignment.isActive)
                // Only active assignments are considered - others were already processed.
                continue;
            assignment.isActive = false;

            if (assignment.exampleRow.isPositive != isEvaluatingPositives)
                // If the assignment is not currently evaluated, we skip it.
                continue;

            if (assignment.exampleRow.decision == null)
                // All assignments must be decided before we can continue with the workflow.
                throw JobException.assignmentUndecided(assignment.id());

            evaluatedRows.add(assignment.exampleRow);
        }

        maxSets = AdjustMaxSets.run(prevMaxSets, evaluatedRows, isEvaluatingPositives);
        LOGGER.debug("Adjusted max sets:\n{}", maxSets);
    }

    private void extendMaxSets() {
        while (
            workflow.state != WorkflowState.FINAL &&
            // We keep doing this until a candidate is generated (or the workflow is finished).
            maxSets.sets().stream().allMatch(set -> set.candidateCount() == 0)
        ) {
            if (workflow.state == WorkflowState.NEGATIVE_EXAMPLES)
                tryExtendNegatives();
            else if (workflow.state == WorkflowState.POSITIVE_EXAMPLES)
                tryExtendPositives();
            else
                // This should not happen.
                throw new RuntimeException("Invalid workflow state: " + workflow.state);
        }

        if (workflow.state != WorkflowState.FINAL)
            LOGGER.debug("Extended max sets:\n{}", maxSets);
        else
            LOGGER.debug("Workflow {} is finished.", workflow.id());
    }

    private void tryExtendNegatives() {
        workflow.lhsSize++;
        if (workflow.lhsSize == maxSets.sets().size()) {
            // We have processed the largest possible lattice elements. Let's continue with positive examples.
            switchToPositives();
            return;
        }

        maxSets = ExtendMaxSets.run(maxSets, workflow.lhsSize);
        final var isAllFinished = maxSets.sets().stream().allMatch(MaxSet::isFinished);
        if (isAllFinished) {
            // The algorithm finished prematurely (giggity). Again, we can continue with positive examples.
            switchToPositives();
        }
    }

    private void tryExtendPositives() {
        workflow.lhsSize--;
        if (workflow.lhsSize == 0) {
            workflow.state = WorkflowState.FINAL;
            return;
        }

        maxSets = ContractMaxSets.run(maxSets, initialMaxSets, workflow.lhsSize);
    }

    private void switchToPositives() {
        // This should be the size of the largest element from any max set that is a positive example.
        // Or 0 if there are none.
        int lhsSize = 0;

        for (int i = 0; i < maxSets.sets().size(); i++) {
            final var initialSet = initialMaxSets.sets().get(i);
            final var currentSet = maxSets.sets().get(i);

            for (final var initial : initialSet.confirmedElements()) {
                if (currentSet.hasConfirmed(initial)) {
                    // We found an initial set element that is still in the current set. That's the definition of a positive example!
                    lhsSize = Math.max(lhsSize, initial.size());
                }
            }
        }

        // If the sets are, in fact, finished, they will be marked as such in the contracting algorithm. So we can just reset them here.
        if (lhsSize != 0)
            maxSets.sets().forEach(set -> set.setIsFinished(false));

        workflow.state = WorkflowState.POSITIVE_EXAMPLES;
        // The positive examples start by decrementing the lhs size, so we need to add 1 to it now.
        workflow.lhsSize = lhsSize + 1;
    }

    private ArmstrongRelation loadPrevAR() {
        // The list is not sorted, but we don't need it to be.
        // We also include all examples (even the inactive ones), because we don't want to repeat their values.
        // Also, at this point, all assignments have been marked as inactive, so ...
        final var exampleRows = assignments.stream()
            .map(assignment -> assignment.exampleRow)
            .toList();

        return new ArmstrongRelation(assignments.get(0).referenceRow, exampleRows);
    }

    private void addNewAssignments(ArmstrongRelation armstrongRelation) {
        // Map current assignments by their lhs, so that we can pair them with the rows from AR.
        final Map<ColumnSet, AssignmentEntity> existingAssignmentsByLhs = assignments.stream().collect(Collectors.toMap(a -> a.exampleRow.lhsSet, a -> a));

        for (final var exampleRow : armstrongRelation.exampleRows) {
            final var existingAssignment = existingAssignmentsByLhs.get(exampleRow.lhsSet);
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

    private void computeViews() {
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
