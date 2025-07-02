package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("exampleRow")
public class ExampleRowEntity {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public String value;

    public ExampleRowEntity(UUID id, String value) {
        this.id = id;
        this.value = value;
    }

    public static ExampleRowEntity create(String value) {
        return new ExampleRowEntity(
            UUID.randomUUID(),
            value
        );
    }

}
