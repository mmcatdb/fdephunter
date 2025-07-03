package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("dataset")
public class DatasetEntity {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public String name;

    public DatasetType type;

    /** Connection string, filename, etc. */
    public String source;

    public static DatasetEntity create(String name, DatasetType type, String source) {
        final var dataset = new DatasetEntity();

        dataset.id = UUID.randomUUID();
        dataset.name = name;
        dataset.type = type;
        dataset.source = source;

        return dataset;
    }

    public static enum DatasetType {
        CSV,
        JSON,
        XML,
        LABELED_GRAPH,
        RELATIONAL,
        ARRAY,
        RDF,
    }

}
