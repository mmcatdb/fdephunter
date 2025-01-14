/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
@Node("Expert")
public class ExpertNode {

    public enum ExpertState {
        PENDING, // The expert hasn't yet accepted nor rejected the assignment.
        UNSUBSCRIBED, // The expert has rejected the assignment.
        IDLE, // The expert has accepted the assignment and now waits for the next negative example.
        ASSIGNED,
        UNAVAILABLE,
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private ExpertState state;

    public static ExpertNode createNew() {
        final var expert = new ExpertNode();
        expert.setState(ExpertState.PENDING);

        return expert;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExpertState getState() {
        return state;
    }

    public void setState(ExpertState state) {
        this.state = state;
    }

}
