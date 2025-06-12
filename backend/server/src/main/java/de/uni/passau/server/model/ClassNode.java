package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/** @deprecated */
@Node("Class")
public class ClassNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String label;

    @Property
    public Double weight;

    public static ClassNode createNew(String label, double weight) {
        final var clazz = new ClassNode();
        clazz.label = label;
        clazz.weight = weight;

        return clazz;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassNode{");
        sb.append("id=").append(id);
        sb.append(", label=").append(label);
        sb.append(", weight=").append(weight);
        sb.append('}');
        return sb.toString();
    }

}
