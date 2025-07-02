package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.repository.DatasetRepository;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private static final Map<UUID, Dataset> CACHE = new TreeMap<>();

    @Autowired
    private DatasetRepository datasetRepository;

    public Dataset getLoadedDataset(DatasetEntity dataset) {
        return CACHE.containsKey(dataset.getId())
            ? getCachedDataset(dataset.getId())
            : createCachedDataset(dataset);
    }

    public Dataset getLoadedDatasetById(UUID id) {
        return CACHE.containsKey(id)
            ? getCachedDataset(id)
            : createCachedDataset(datasetRepository.findById(id).get());
    }

    private Dataset getCachedDataset(UUID id) {
        final var dataset = CACHE.get(id);
        if (!dataset.isLoaded())
            dataset.load();

        return dataset;
    }

    private Dataset createCachedDataset(DatasetEntity dataset) {
        final Dataset datasetObject = createDataset(dataset);
        datasetObject.load();

        CACHE.put(dataset.getId(), datasetObject);

        return datasetObject;
    }

    private Dataset createDataset(DatasetEntity dataset) {
        switch (dataset.type) {
            case ARRAY ->
                throw new UnsupportedOperationException("Not supported yet.");
            case CSV -> {
                LOGGER.warn("TODO: EXTEND METADATA -- ADD PROPERTY CONTAINS HEADER");
                return new CSVDataset(dataset.source, true);
            }
            case JSON ->
                throw new UnsupportedOperationException("Not supported yet.");
            case LABELED_GRAPH ->
                throw new UnsupportedOperationException("Not supported yet.");
            case RDF ->
                throw new UnsupportedOperationException("Not supported yet.");
            case RELATIONAL ->
                throw new UnsupportedOperationException("Not supported yet.");
            case XML ->
                throw new UnsupportedOperationException("Not supported yet.");
            default ->
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
