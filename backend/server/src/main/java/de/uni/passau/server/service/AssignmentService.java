package de.uni.passau.server.service;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.core.example.ExampleDecision.DecisionColumn;
import de.uni.passau.core.example.ExampleDecision.DecisionColumnStatus;
import de.uni.passau.core.example.ExampleDecision.DecisionStatus;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public void acceptAllAssignments(WorkflowEntity workflow) {
        final var isEvaluatingPositives = workflow.state == WorkflowState.POSITIVE_EXAMPLES;
        final var openAssignments = assignmentRepository.findAllByWorkflowId(workflow.getId()).stream()
            .filter(assignment -> assignment.exampleRow.decision == null || assignment.exampleRow.isPositive == isEvaluatingPositives)
            // TODO something like which assignments are active in the workflow
            .filter(assignment -> assignment.exampleRow.lhsSet.size() == workflow.iteration)
            .toList();

        for (final var assignment : openAssignments) {
            final var columns = new ArrayList<DecisionColumn>();

            for (int i = 0; i < assignment.columns.length; i++) {
                final var isEvaluating = assignment.exampleRow.rhsSet.get(i);
                columns.add(new DecisionColumn(isEvaluating ? DecisionColumnStatus.VALID : null, new ArrayList<String>()));
            }

            assignment.exampleRow.decision = new ExampleDecision(DecisionStatus.ACCEPTED, columns.toArray(DecisionColumn[]::new));
        }

        assignmentRepository.saveAll(openAssignments);
    }

}
