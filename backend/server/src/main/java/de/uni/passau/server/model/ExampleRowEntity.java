package de.uni.passau.server.model;

import org.springframework.data.annotation.Id;

public class ExampleRowEntity {

    @Id
    public String id;

    public String value;

    public ExampleRowEntity(String id, String value) {
        this.id = id;
        this.value = value;
    }

}
