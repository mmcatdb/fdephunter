package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.repository.DatasetRepository;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private static final Map<String, Dataset> CACHE = new TreeMap<>();

    @Autowired
    private DatasetRepository datasetRepository;

    public Dataset getLoadedDataset(DatasetNode datasetNode) {
        return CACHE.containsKey(datasetNode.name)
            ? getCachedDataset(datasetNode.name)
            : createCachedDataset(datasetNode);
    }

    public Dataset getLoadedDatasetByName(String name) {
        return CACHE.containsKey(name)
            ? getCachedDataset(name)
            : createCachedDataset(datasetRepository.getDatasetByName(name));
    }

    private Dataset getCachedDataset(String name) {
        final var dataset = CACHE.get(name);
        if (!dataset.isLoaded())
            dataset.load();

        return dataset;
    }

    private Dataset createCachedDataset(DatasetNode node) {
        final Dataset dataset = createDataset(node);
        dataset.load();

        CACHE.put(node.name, dataset);

        return dataset;
    }

    private Dataset createDataset(DatasetNode node) {
        switch (node.type) {
            case ARRAY ->
                throw new UnsupportedOperationException("Not supported yet.");
            case CSV -> {
                LOGGER.warn("TODO: EXTEND METADATA -- ADD PROPERTY CONTAINS HEADER");
                return new CSVDataset(node.source, true);
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
