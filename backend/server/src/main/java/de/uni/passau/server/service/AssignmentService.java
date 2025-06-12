package de.uni.passau.server.service;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.AssignmentNode.AssignmentState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.NegativeExampleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private NegativeExampleRepository negativeExampleRepository;

    public Mono<AssignmentNode> createAssignment(String exampleId) {
        final var newAssignment = AssignmentNode.createNew();

        return assignmentRepository.save(newAssignment).flatMap(assignment ->
            assignmentRepository.saveBelongsToExample(assignment.getId(), exampleId)
                .switchIfEmpty(Mono.error(new RuntimeException("BELONGS_TO_EXAMPLE")))
                .then(Mono.just(assignment))
        );
    }

    public Mono<Void> evaluateAssignment(String assignmentId, ExampleDecision decisionObject) {
        // FIXME After flux is removed.
        // final var assignment = assignmentRepository.findById(assignmentId);
        // assignment.decision = decisionObject;
        // assignment.state = toState(decisionObject.status());
        // assignmentRepository.save(assignment)

        final AssignmentState state = toState(decisionObject.status());

        return assignmentRepository.evaluateAssignment(assignmentId, state, "TODO")
            .then(negativeExampleRepository.updateState(assignmentId))
            .then();
    }

    private AssignmentState toState(ExampleDecision.DecisionStatus status) {
        return switch (status) {
            case ACCEPTED -> AssignmentState.ACCEPTED;
            case REJECTED -> AssignmentState.REJECTED;
            default -> AssignmentState.DONT_KNOW;
        };
    }

}
