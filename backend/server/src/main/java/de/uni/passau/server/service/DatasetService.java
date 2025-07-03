package de.uni.passau.server.service;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DatasetEntity.DatasetType;
import de.uni.passau.server.repository.DatasetRepository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    @Autowired
    private DatasetRepository datasetRepository;

    public Dataset getLoadedDatasetById(UUID id) {
        final var entity = datasetRepository.findById(id).get();
        final var dataset = getSpecificDataset(entity);
        if (!dataset.isLoaded())
            dataset.load();

        return dataset;
    }

    private Dataset getSpecificDataset(DatasetEntity entity) {
        return switch (entity.type) {
            case ARRAY -> throw new UnsupportedOperationException(DatasetType.ARRAY + " dataset not supported yet.");
            case CSV -> new CSVDataset(entity.source, true);
            case JSON -> throw new UnsupportedOperationException(DatasetType.JSON + " dataset not supported yet.");
            case LABELED_GRAPH -> throw new UnsupportedOperationException(DatasetType.LABELED_GRAPH + " dataset not supported yet.");
            case RDF -> throw new UnsupportedOperationException(DatasetType.RDF + " dataset not supported yet.");
            case RELATIONAL -> throw new UnsupportedOperationException(DatasetType.RELATIONAL + " dataset not supported yet.");
            case XML -> throw new UnsupportedOperationException(DatasetType.XML + " dataset not supported yet.");
        };
    }

}
