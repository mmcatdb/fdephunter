package de.uni.passau.server.controller;

import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DatasetEntity.DatasetType;
import de.uni.passau.server.repository.DatasetRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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

        List<DatasetEntity> datasets = new ArrayList<>();

        datasets.add(DatasetEntity.create("iris", DatasetType.CSV, "../data/iris.csv"));
        datasets.add(DatasetEntity.create("imdb sample", DatasetType.CSV, "../data/imdb-title-sample.csv"));
        // NICE_TO_HAVE: Add more datasets here.
        // datasets.add(DatasetEntity.create("balance-scale", DatasetType.CSV, "data/balance-scale.csv"));
        // datasets.add(DatasetEntity.create("chess", DatasetType.CSV, "data/chess.csv"));
        // datasets.add(DatasetEntity.create("abalone", DatasetType.CSV, "data/abalone.csv"));
        // datasets.add(DatasetEntity.create("nursery", DatasetType.CSV, "data/nursery.csv"));
        // datasets.add(DatasetEntity.create("breast-cancer-wisconsin", DatasetType.CSV, "data/breast-cancer-wisconsin.csv"));
        // datasets.add(DatasetEntity.create("bridges", DatasetType.CSV, "data/bridges.csv"));
        // datasets.add(DatasetEntity.create("echocardiogram", DatasetType.CSV, "data/echocardiogram.csv"));
        // datasets.add(DatasetEntity.create("adult", DatasetType.CSV, "data/adult.csv"));
        // datasets.add(DatasetEntity.create("letter", DatasetType.CSV, "data/letter.csv"));
        // datasets.add(DatasetEntity.create("ncvoter", DatasetType.CSV, "data/ncvoter.csv"));
        // datasets.add(DatasetEntity.create("hepatitis", DatasetType.CSV, "data/hepatitis.csv"));
        // datasets.add(DatasetEntity.create("horse", DatasetType.CSV, "data/horse.csv"));
        // datasets.add(DatasetEntity.create("fd-reduced-30", DatasetType.CSV, "data/fd-reduced-30.csv"));
        // datasets.add(DatasetEntity.create("plista", DatasetType.CSV, "data/plista.csv"));
        // datasets.add(DatasetEntity.create("flight", DatasetType.CSV, "data/flight.csv"));
        // datasets.add(DatasetEntity.create("uniprot", DatasetType.CSV, "data/uniprot.csv"));
        // datasets.add(DatasetEntity.create("lineitem", DatasetType.CSV, "data/lineitem.csv"));

        datasetRepository.saveAll(datasets);

        LOGGER.info("Datasets initialized: {}", datasets.size());
    }

}
