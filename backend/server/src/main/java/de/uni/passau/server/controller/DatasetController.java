package de.uni.passau.server.controller;

import de.uni.passau.server.controller.response.DatasetData;
import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.service.DatasetService;
import de.uni.passau.server.service.WorkflowService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/datasets")
    public Flux<DatasetNode> getAllDatasets() {
        return datasetService.getAllDatasets();
    }

    @GetMapping("/datasets/workflows/{workflowId}/data")
    public Mono<DatasetData> getDataForWorkflow(@PathVariable String workflowId, @RequestParam(required = false, defaultValue = "10") String limit) {
        int numberLimit = tryParseLimit(limit);

        return workflowService.getDatasetName(workflowId)
            .flatMap(name -> datasetService.getLoadedDatasetByName(name))
            .map(dataset -> new DatasetData(dataset.getHeader(), dataset.getRows().stream().limit(numberLimit).toList()));
    }

    // @GetMapping("/datasets/assignments/{assignmentId}/data")
    // public Mono<DatasetData> getDataForAssignment(@PathVariable String assignmentId, @RequestParam(required = false, defaultValue = "10") String limit) {
    //     int numberLimit = tryParseLimit(limit);

    //     return assignmentService.getDatasetName(assignmentId)
    //         .flatMap(name -> datasetService.getLoadedDatasetByName(name))
    //         .map(dataset -> new DatasetData(dataset.getHeader(), dataset.getRows().stream().limit(numberLimit).toList()));
    // }

    private static final int defaultLimit = 10;

    public static int tryParseLimit(String limit) {
        try {
            return Integer.parseInt(limit);
        }
        catch (NumberFormatException e) {
            return defaultLimit;
        }
    }

}
