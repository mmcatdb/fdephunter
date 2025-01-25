package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("DiscoveryResult")
public class DiscoveryResultNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String payload;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiscoveryResultNode{");
        sb.append("id=").append(id);
        sb.append(", payload=").append(payload);
        sb.append('}');
        return sb.toString();
    }

}
