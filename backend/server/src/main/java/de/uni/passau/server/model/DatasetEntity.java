package de.uni.passau.server.model;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
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

    /** Connection string, filename, etc. */
    public String source;

    /** Human-readable name. Unique. */
    public String name;

    /** The name under which we tried to create the dataset. Should be the same as {@name} unless this {@name} was already taken. */
    public @Nullable String originalName;

    public static DatasetEntity create(DatasetType type, String source, String name, @Nullable String originalName) {
        final var dataset = new DatasetEntity();

        dataset._id = UUID.randomUUID();
        dataset.type = type;
        dataset.source = source;
        dataset.name = name;
        dataset.originalName = originalName;

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
