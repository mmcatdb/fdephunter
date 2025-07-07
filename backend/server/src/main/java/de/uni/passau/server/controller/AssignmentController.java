package de.uni.passau.server.controller;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.repository.AssignmentRepository;

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
public class AssignmentController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    private AssignmentRepository assignmentRepository;

    @GetMapping("/assignments/{assignmentId}")
    public AssignmentEntity getAssignment(@PathVariable UUID assignmentId) {
        return assignmentRepository.findById(assignmentId).get();
    }

    @GetMapping("/workflows/{workflowId}/assignments")
    public List<AssignmentEntity> getAssignments(@PathVariable UUID workflowId) {
        return assignmentRepository.findAllByWorkflowIdAndIsActive(workflowId, true);
    }

    @PostMapping("/assignments/{assignmentId}/evaluate")
    public AssignmentEntity evaluateAssignment(@PathVariable String assignmentId, @RequestBody ExampleDecision decision) {
        AssignmentEntity assignment = assignmentRepository.findById(UUID.fromString(assignmentId)).get();

        assignment.exampleRow.decision = decision;

        assignmentRepository.save(assignment);

        return assignment;
    }

    @PostMapping("/assignments/{assignmentId}/reset")
    public AssignmentEntity resetAssignment(@PathVariable String assignmentId, @RequestBody ExampleDecision decision) {
        AssignmentEntity assignment = assignmentRepository.findById(UUID.fromString(assignmentId)).get();

        assignment.exampleRow.decision = null;

        assignmentRepository.save(assignment);

        return assignment;
    }

}
