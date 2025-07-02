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

    public DatasetEntity(UUID id, String name, DatasetType type, String source) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public static DatasetEntity create(String name, DatasetType type, String source) {
        return new DatasetEntity(
            UUID.randomUUID(),
            name,
            type,
            source
        );
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
