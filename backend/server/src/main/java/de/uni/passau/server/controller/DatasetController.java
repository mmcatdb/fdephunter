package de.uni.passau.server.controller;

import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.repository.DatasetRepository;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.DatasetService;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatasetController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @GetMapping("/datasets")
    public List<DatasetEntity> getDatasets() {
        return datasetRepository.findAll();
    }

    public record DatasetData(String[] header, List<String[]> rows) implements Serializable {}

    @GetMapping("/workflows/{workflowId}/data")
    public DatasetData getDatasetData(
        @PathVariable UUID workflowId,
        @RequestParam(required = false, defaultValue = "0") String offset,
        @RequestParam(required = false, defaultValue = "100") String limit
    ) {
        final int numberOffset = Integer.parseInt(offset);
        final int numberLimit = Integer.parseInt(limit);

        final var workflow = workflowRepository.findById(workflowId).get();
        final var dataset = datasetService.getLoadedDatasetById(workflow.datasetId);

        System.out.println(numberOffset + "-" + numberLimit);
        System.out.println("size: " + dataset.getRows().size());

        return new DatasetData(dataset.getHeader(), dataset.getRows().stream().skip(numberOffset).limit(numberLimit).toList());
    }

    // TODO
    public void UploadDataset() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
