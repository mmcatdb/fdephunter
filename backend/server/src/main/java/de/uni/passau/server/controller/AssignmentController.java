package de.uni.passau.server.controller;

import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.server.controller.response.AssignmentResponse;
import de.uni.passau.server.controller.response.DatasetData;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.service.AssignmentService;
import de.uni.passau.server.service.DatasetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AssignmentController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private DatasetService datasetService;

    @GetMapping("/assignments/{assignmentId}")
    public Mono<AssignmentResponse> getAssignment(@PathVariable String assignmentId, @RequestParam(required = false, defaultValue = "5") String limit) {
        int numberLimit = DatasetController.tryParseLimit(limit);
        return assignmentRepository.findGroupById(assignmentId).flatMap(assignmentGroup ->
            assignmentRepository.getDatasetName(assignmentId)
                .flatMap(name -> datasetService.getLoadedDatasetByName(name))
                .map(dataset -> DatasetData.fromNodes(dataset, numberLimit))
                .map(datasetData -> AssignmentResponse.fromNodes(assignmentGroup, datasetData))
        );
    }

    @PostMapping("/assignments/{assignmentId}/evaluate")
    public Mono<AssignmentResponse> evaluateAssignment(
        @PathVariable String assignmentId,
        @RequestBody ExampleDecision decision,
        @RequestParam(required = false, defaultValue = "5") String limit
    ) {
        return assignmentService.evaluateAssignment(assignmentId, decision)
            .then(getAssignment(assignmentId, limit));
    }

}
