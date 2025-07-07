package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document("dataset")
public class DatasetEntity {

    @Id @JsonProperty("id")
    private UUID _id;

    public UUID id() {
        return _id;
    }

    public DatasetType type;

    public String name;

    /** Connection string, filename, etc. */
    public String source;

    public static DatasetEntity create(DatasetType type, String name, String source) {
        final var dataset = new DatasetEntity();

        dataset._id = UUID.randomUUID();
        dataset.type = type;
        dataset.name = name;
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
