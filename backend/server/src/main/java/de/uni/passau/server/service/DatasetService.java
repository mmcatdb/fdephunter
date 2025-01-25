package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.repository.DatasetRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatasetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetService.class);

    private static final Map<String, Dataset> CACHE = new TreeMap<>();

    @Autowired
    private DatasetRepository datasetRepository;

    public Flux<DatasetNode> getAllDatasets() {
        return datasetRepository.findAll();
    }

    public Mono<Void> removeWorkflow(Long id) {
        return datasetRepository.deleteById(id);
    }

    public Mono<DatasetNode> save(DatasetNode dataset) {
        return datasetRepository.save(dataset);
    }

    public Flux<DatasetNode> saveAll(List<DatasetNode> datasets) {
        return datasetRepository.saveAll(datasets);
    }

    public Mono<DatasetNode> getDatasetByName(String name) {
        return datasetRepository.getDatasetByName(name);
    }

    public Dataset getLoadedDataset(DatasetNode datasetNode) {
        return CACHE.containsKey(datasetNode.getName())
            ? getCachedDataset(datasetNode.getName())
            : createCachedDataset(datasetNode);
    }

    public Mono<Dataset> getLoadedDatasetByName(String name) {
        return CACHE.containsKey(name)
            ? Mono.just(getCachedDataset(name))
            : getDatasetByName(name).map(this::createCachedDataset);
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

        CACHE.put(node.getName(), dataset);

        return dataset;
    }

    private Dataset createDataset(DatasetNode node) {
        switch (node.getType()) {
            case ARRAY ->
                throw new UnsupportedOperationException("Not supported yet.");
            case CSV -> {
                LOGGER.warn("TODO: EXTEND METADATA -- ADD PROPERTY CONTAINS HEADER");
                return new CSVDataset(node.getSource(), true);
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
