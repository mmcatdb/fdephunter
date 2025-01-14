/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import java.util.Date;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
@Node("Assignment")
public class AssignmentNode {

    public enum ExpertVerdict {
        NEW,
        ACCEPTED,
        REJECTED,
        I_DONT_KNOW,
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String decision;

    @Property
    private ExpertVerdict verdict;

    @Property
    private Long createdAt;

    public static AssignmentNode createNew() {
        final var assignment = new AssignmentNode();
        assignment.setVerdict(ExpertVerdict.NEW);
        assignment.setCreatedAt(new Date().getTime());

        return assignment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public ExpertVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(ExpertVerdict verdict) {
        this.verdict = verdict;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

}
