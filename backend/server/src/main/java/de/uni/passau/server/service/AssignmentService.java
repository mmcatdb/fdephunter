package de.uni.passau.server.service;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.repository.AssignmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public AssignmentNode createAssignment(String exampleId) {
        var assignment = AssignmentNode.createNew();

        assignment = assignmentRepository.save(assignment);

        assignmentRepository.saveBelongsToExample(assignment.getId(), exampleId);

        return assignment;
    }

    public void evaluateAssignment(String assignmentId, ExampleDecision decisionObject) {
        var assignment = assignmentRepository.findById(assignmentId).get();

        assignment.decision = decisionObject;
        // assignment.state = toState(decisionObject.status());

        assignment = assignmentRepository.save(assignment);
    }

}
