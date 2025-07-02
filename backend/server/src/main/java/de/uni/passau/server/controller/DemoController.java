package de.uni.passau.server.controller;

import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.repository.DatasetRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @PostMapping("/demo/initialize")
    public String initialize() {
        return initializeDatabase();
    }

    private String initializeDatabase() {
        var workflow = WorkflowNode.createNew();

        // remove all data from database
        workflowRepository.purgeDatabase();

        // initialize datasets
        LOGGER.info("Initializing datasets.");

        List<DatasetNode> datasets = new ArrayList<>();

        datasets.add(new DatasetNode("iris", DatasetNode.DatasetType.CSV, "data/iris.csv", "iris", null, null, null, null));
        datasets.add(new DatasetNode("balance-scale", DatasetNode.DatasetType.CSV, "data/balance-scale.csv", "balance-scale", null, null, null, null));
        datasets.add(new DatasetNode("chess", DatasetNode.DatasetType.CSV, "data/chess.csv", "chess", null, null, null, null));
        datasets.add(new DatasetNode("abalone", DatasetNode.DatasetType.CSV, "data/abalone.csv", "abalone", null, null, null, null));
        datasets.add(new DatasetNode("nursery", DatasetNode.DatasetType.CSV, "data/nursery.csv", "nursery", null, null, null, null));
        datasets.add(new DatasetNode("breast-cancer-wisconsin", DatasetNode.DatasetType.CSV, "data/breast-cancer-wisconsin.csv", "breast-cancer-wisconsin", null, null, null, null));
        datasets.add(new DatasetNode("bridges", DatasetNode.DatasetType.CSV, "data/bridges.csv", "bridges", null, null, null, null));
        datasets.add(new DatasetNode("echocardiogram", DatasetNode.DatasetType.CSV, "data/echocardiogram.csv", "echocardiogram", null, null, null, null));
        datasets.add(new DatasetNode("adult", DatasetNode.DatasetType.CSV, "data/adult.csv", "adult", null, null, null, null));
        datasets.add(new DatasetNode("letter", DatasetNode.DatasetType.CSV, "data/letter.csv", "letter", null, null, null, null));
        datasets.add(new DatasetNode("ncvoter", DatasetNode.DatasetType.CSV, "data/ncvoter.csv", "ncvoter", null, null, null, null));
        datasets.add(new DatasetNode("hepatitis", DatasetNode.DatasetType.CSV, "data/hepatitis.csv", "hepatitis", null, null, null, null));
        datasets.add(new DatasetNode("horse", DatasetNode.DatasetType.CSV, "data/horse.csv", "horse", null, null, null, null));
        datasets.add(new DatasetNode("fd-reduced-30", DatasetNode.DatasetType.CSV, "data/fd-reduced-30.csv", "fd-reduced-30", null, null, null, null));
        datasets.add(new DatasetNode("plista", DatasetNode.DatasetType.CSV, "data/plista.csv", "plista", null, null, null, null));
        datasets.add(new DatasetNode("flight", DatasetNode.DatasetType.CSV, "data/flight.csv", "flight", null, null, null, null));
        datasets.add(new DatasetNode("uniprot", DatasetNode.DatasetType.CSV, "data/uniprot.csv", "uniprot", null, null, null, null));
        datasets.add(new DatasetNode("lineitem", DatasetNode.DatasetType.CSV, "data/lineitem.csv", "lineitem", null, null, null, null));

        datasetRepository.saveAll(datasets);

        // initialize workflow
        LOGGER.info("Initializing workflow.");
        workflow = workflowRepository.save(workflow);

        return "{\"message\" : \"ok\"}";
    }

}
