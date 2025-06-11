package de.uni.passau.server.model.entity;

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

    public enum NegativeExampleState {
        NEW,
        REJECTED,
        ACCEPTED,
        ANSWERED,
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NegativeExampleNode{");
        sb.append("id=").append(id);
        sb.append(", payload=").append(payload);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    public static record Payload(
        Map<String, String> innerValues,
        Map<String, String> originalValues,
        List<String> view,
        List<FDInit> fds,
        Map<String, String> values
    ) {}

}
