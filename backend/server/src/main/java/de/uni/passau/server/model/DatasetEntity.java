package de.uni.passau.server.model;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Document("dataset")
public class DatasetEntity {

    @Id @JsonProperty("id")
    private UUID _id;

    public UUID id() {
        return _id;
    }

    public DatasetSettings settings;

    /** Connection string, filename, etc. */
    public String source;

    /** Human-readable name. Unique. */
    public String name;

    /** The name under which we tried to create the dataset. Should be the same as {@name} unless this {@name} was already taken. */
    public @Nullable String originalName;

    public static DatasetEntity create(DatasetSettings settings, String source, String name, @Nullable String originalName) {
        final var dataset = new DatasetEntity();

        dataset._id = UUID.randomUUID();
        dataset.settings = settings;
        dataset.source = source;
        dataset.name = name;
        dataset.originalName = originalName;

        return dataset;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = CsvSettings.class, name = "CSV"),
    })
    public interface DatasetSettings {}

    public record CsvSettings(
        boolean hasHeader,
        char separator
    ) implements DatasetSettings {}

}
