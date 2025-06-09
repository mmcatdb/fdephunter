package de.uni.passau.server.service;

import de.uni.passau.core.nex.Decision;
import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.AssignmentNode.AssignmentVerdict;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.AssignmentRepository.AssignmentNodeGroup;
import de.uni.passau.server.repository.NegativeExampleRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private NegativeExampleRepository negativeExampleRepository;

    public Flux<AssignmentNode> findAllBelongsToExample(String exampleId) {
        return assignmentRepository.findAllBelongsToExample(exampleId);
    }

    public Mono<AssignmentNodeGroup> findGroupById(String assignmentId) {
        return assignmentRepository.findGroupById(assignmentId);
    }

    public Mono<AssignmentNode> createAssignment(String exampleId) {
        final var newAssignment = AssignmentNode.createNew();

        return assignmentRepository.save(newAssignment).flatMap(assignment ->
            assignmentRepository.saveBelongsToExample(assignment.getId(), exampleId)
                .switchIfEmpty(Mono.error(new RuntimeException("BELONGS_TO_EXAMPLE")))
                .then(Mono.just(assignment))
        );
    }

    public Mono<Void> evaluateAssignment(String assignmentId, Decision decisionObject) {
        try {
            final String decision = Decision.jsonWriter.writeValueAsString(decisionObject);
            final AssignmentVerdict verdict = toVerdict(decisionObject.getStatus());

            return assignmentRepository.evaluateAssignment(assignmentId, verdict, decision)
                .then(negativeExampleRepository.updateState(assignmentId))
                .then();
        }
        catch (JsonProcessingException ex) {
            throw new UnsupportedOperationException("EVALUATE_ASSIGNMENT_FAILED_ERROR_TODO");
        }
    }

    private AssignmentVerdict toVerdict(Decision.Status status) {
        return switch (status) {
            case ACCEPTED -> AssignmentVerdict.ACCEPTED;
            case REJECTED -> AssignmentVerdict.REJECTED;
            default -> AssignmentVerdict.DONT_KNOW;
        };
    }

    public Mono<String> getDatasetName(String assignmentId) {
        return assignmentRepository.getDatasetName(assignmentId);
    }

}
