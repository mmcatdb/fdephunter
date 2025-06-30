package de.uni.passau.server.model;

import java.util.Date;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import de.uni.passau.core.example.ExampleDecision;

@Node("Assignment")
public class AssignmentNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    // FIXME Probably some value conversion is needed here.
    @Property
    public ExampleDecision decision;

    // @Property
    // public ExampleState state;

    @Property
    public int rowIndex;

    @Property
    public Long createdAt;

    public static AssignmentNode createNew() {
        final var assignment = new AssignmentNode();
        // assignment.state = ExampleState.NEW;
        assignment.createdAt = new Date().getTime();

        return assignment;
    }

}
