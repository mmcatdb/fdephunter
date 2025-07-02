package de.uni.passau.server.controller;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.server.controller.response.AssignmentResponse;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.service.AssignmentService;

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

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/assignments/{assignmentId}")
    public AssignmentResponse getAssignment(@PathVariable String assignmentId) {
        final var assignmentGroup = assignmentRepository.findGroupById(assignmentId);

        // return AssignmentResponse.fromNodes(assignmentGroup, datasetData);
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping("/workflows/{workflowId}/assignments")
    public List<AssignmentResponse> getAssignments(@PathVariable UUID workflowId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @PostMapping("/assignments/{assignmentId}/evaluate")
    public AssignmentResponse evaluateAssignment(@PathVariable String assignmentId, @RequestBody ExampleDecision decision) {
        assignmentService.evaluateAssignment(assignmentId, decision);
        return getAssignment(assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/reset")
    public AssignmentResponse resetAssignment(@PathVariable String assignmentId, @RequestBody ExampleDecision decision) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
