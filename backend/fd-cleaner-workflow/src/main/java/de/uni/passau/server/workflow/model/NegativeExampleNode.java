/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import de.uni.passau.core.approach.FDInit;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/**
 *
 * @author pavel.koupil
 */
@Node("NegativeExample")
public class NegativeExampleNode {

    public enum NegativeExampleState {
        NEW,
        REJECTED,
        ACCEPTED,
        ANSWERED,
        CONFLICT,
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String payload;

    @Property
    private NegativeExampleState state;

    public static NegativeExampleNode createNew(String id, String payload) {
        final var example = new NegativeExampleNode();
        example.setId(id);
        example.setPayload(payload);
        example.setState(NegativeExampleState.NEW);

        return example;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public NegativeExampleState getState() {
        return state;
    }

    public void setState(NegativeExampleState state) {
        this.state = state;
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
