package de.uni.passau.server.model;

import de.uni.passau.core.approach.FDInit;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/** @deprecated */
@Node("NegativeExample")
public class NegativeExampleNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String payload;

    @Property
    public NegativeExampleState state;

    public static NegativeExampleNode createNew(String id, String payload) {
        final var example = new NegativeExampleNode();
        example.id = id;
        example.payload = payload;
        example.state = NegativeExampleState.NEW;

        return example;
    }

    public static record Payload(
        Map<String, String> innerValues,
        Map<String, String> originalValues,
        List<String> view,
        List<FDInit> fds,
        Map<String, String> values
    ) {}

    public enum NegativeExampleState {
        NEW,
        REJECTED,
        ACCEPTED,
        ANSWERED,
    }

}
