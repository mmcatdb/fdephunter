package de.uni.passau.server.controller;

import de.uni.passau.server.controller.response.DatasetData;
import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.DatasetRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.DatasetService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @GetMapping("/datasets")
    public List<DatasetNode> getAllDatasets() {
        return datasetRepository.findAll();
    }

    @GetMapping("/datasets/workflows/{workflowId}/data")
    public DatasetData getDataForWorkflow(@PathVariable String workflowId, @RequestParam(required = false, defaultValue = "10") String limit) {
        final int numberLimit = tryParseLimit(limit);

        final var datasetName = workflowRepository.getDatasetName(workflowId);
        final var dataset = datasetService.getLoadedDatasetByName(datasetName);

        return new DatasetData(dataset.getHeader(), dataset.getRows().stream().limit(numberLimit).toList());
    }

    @GetMapping("/datasets/assignments/{assignmentId}/data")
    public DatasetData getDataForAssignment(@PathVariable String assignmentId, @RequestParam(required = false, defaultValue = "10") String limit) {
        final int numberLimit = tryParseLimit(limit);

        final var datasetName = assignmentRepository.getDatasetName(assignmentId);
        final var dataset = datasetService.getLoadedDatasetByName(datasetName);

        return new DatasetData(dataset.getHeader(), dataset.getRows().stream().limit(numberLimit).toList());
    }

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
