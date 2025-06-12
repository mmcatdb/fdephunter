package de.uni.passau.server.model;

import java.util.Date;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Assignment")
public class AssignmentNode {

    public enum AssignmentState {
        NEW,
        ACCEPTED,
        REJECTED,
        DONT_KNOW,
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String decision;

    @Property
    public AssignmentState state;

    @Property
    public int rowIndex;

    @Property
    public Long createdAt;

    public static AssignmentNode createNew() {
        final var assignment = new AssignmentNode();
        assignment.state = AssignmentState.NEW;
        assignment.createdAt = new Date().getTime();

        return assignment;
    }

}
