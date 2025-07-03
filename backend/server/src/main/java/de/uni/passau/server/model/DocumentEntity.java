package de.uni.passau.server.model;

import org.bson.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Any potentially large json thing that needs to be persisted.
 * I.e., job results.
 */
@org.springframework.data.mongodb.core.mapping.Document("document")
public class DocumentEntity {

    // We use string here because we need to create the id specifically for each document.
    @Id @JsonProperty("id")
    private String _id;

    public String id() {
        return _id;
    }

    private Document data;

    @Transient
    private Object parsedData;

    public <T> T getParsedData(Class<T> type, ObjectMapper objectMapper) {
        return objectMapper.convertValue(data, type);
    }

    // There is no need to update, the data is just replaced.
    private void setParsedData(Object data, ObjectMapper objectMapper) {
        try {
            this.parsedData = data;
            this.data = Document.parse(objectMapper.writeValueAsString(data));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to serialize data to BSON Document", e);
        }
    }

    /** Use this directly as a response to frontend. */
    public Document toResponse() {
        return data;
    }

    public static DocumentEntity create(String id, Object data, ObjectMapper objectMapper) {
        final var document = new DocumentEntity();

        document._id = id;
        document.setParsedData(data, objectMapper);

        return document;
    }

}
