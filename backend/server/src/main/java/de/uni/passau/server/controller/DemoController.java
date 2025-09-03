package de.uni.passau.server.controller;

import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import de.uni.passau.core.model.MaxSets;
import de.uni.passau.server.Configuration.ServerProperties;
import de.uni.passau.server.model.DatasetEntity.CsvSettings;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.service.DatasetService;
import de.uni.passau.server.service.StorageService;

@RestController
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ServerProperties server;

    @GetMapping("/demo")
    public String demo() {
        final var a = Paths.get(server.datasetDirectory(), "iris.csv");
        System.out.println("Dataset path: " + a.toAbsolutePath());
        System.out.println("Dataset path: " + a.toString());

        return a.toString();
    }

    @GetMapping("/demo/{workflowId}/maxsets")
    public String demoMaxsets(@PathVariable UUID workflowId) {
        final var maxSets = storageService.get(WorkflowEntity.maxSetsId(workflowId), MaxSets.class);

        return "<pre>" + maxSets.toString() + "</pre>";
    }

    @PostMapping("/demo/reset-database")
    public String resetDatabase() {
        purgeDatabase();
        initializeDatasets();

        return "{\"message\": \"ok\"}";
    }

    private void purgeDatabase() {
        LOGGER.info("Purging database.");

        for (final var collectionName : mongoTemplate.getCollectionNames()) {
            LOGGER.info("Dropping collection: {}", collectionName);
            mongoTemplate.dropCollection(collectionName);
        }

        LOGGER.info("Database purged.");
    }

    private void initializeDatasets() {
        LOGGER.info("Initializing datasets.");

        // Make sure the names are unique. It's not checked neither in the service nor in the database.
        datasetService.createDataset(new CsvSettings(true, ','), "imdb (running example)", "imdb-title-sample.csv");


        datasetService.createDataset(new CsvSettings(true, ','), "airline (13 cols, 168 fds)", "airline_sample_168fds_13col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "animal (9 cols, 66 fds)", "animal_sample_66fds_9col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "circuits (8 cols, 47 fds)", "circuits_sample_47fds_8col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "flights (19 cols, 1374 fds)", "flights_sample_1374fds_19col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "F1 drivers (8 cols, 33 fds)", "drivers_sample_33fds_8col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "F1 races (16 cols, 196 fds)", "races_sample_196fds_16col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "F1 results (11 cols, 100 fds)", "results_sample_100fds_11col.csv");
        datasetService.createDataset(new CsvSettings(true, ','), "iris (metanome)", "iris.csv");
        // NICE_TO_HAVE: Add more datasets here.
        // datasetService.createDataset(DatasetType.CSV, "balance-scale", "balance-scale.csv");
        // datasetService.createDataset(DatasetType.CSV, "chess", "chess.csv");
        // datasetService.createDataset(DatasetType.CSV, "abalone", "abalone.csv");
        // datasetService.createDataset(DatasetType.CSV, "nursery", "nursery.csv");
        // datasetService.createDataset(DatasetType.CSV, "breast-cancer-wisconsin", "breast-cancer-wisconsin.csv");
        // datasetService.createDataset(DatasetType.CSV, "bridges", "bridges.csv");
        // datasetService.createDataset(DatasetType.CSV, "echocardiogram", "echocardiogram.csv");
        // datasetService.createDataset(DatasetType.CSV, "adult", "adult.csv");
        // datasetService.createDataset(DatasetType.CSV, "letter", "letter.csv");
        // datasetService.createDataset(DatasetType.CSV, "ncvoter", "ncvoter.csv");
        // datasetService.createDataset(DatasetType.CSV, "hepatitis", "hepatitis.csv");
        // datasetService.createDataset(DatasetType.CSV, "horse", "horse.csv");
        // datasetService.createDataset(DatasetType.CSV, "fd-reduced-30", "fd-reduced-30.csv");
        // datasetService.createDataset(DatasetType.CSV, "plista", "plista.csv");
        // datasetService.createDataset(DatasetType.CSV, "flight", "flight.csv");
        // datasetService.createDataset(DatasetType.CSV, "uniprot", "uniprot.csv");
        // datasetService.createDataset(DatasetType.CSV, "lineitem", "lineitem.csv");

        LOGGER.info("Datasets initialized.");
    }

}
