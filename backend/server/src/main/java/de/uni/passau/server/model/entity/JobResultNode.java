package de.uni.passau.server.model.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("JobResult")
public class JobResultNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String payload;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JobResultNode{");
        sb.append("id=").append(id);
        sb.append(", payload=").append(payload);
        sb.append('}');
        return sb.toString();
    }

}
