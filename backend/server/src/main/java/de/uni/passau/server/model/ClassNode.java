package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Class")
public class ClassNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String label;

    @Property
    private Double weight;

    public static ClassNode createNew(String label, double weight) {
        final var classX = new ClassNode();
        classX.setLabel(label);
        classX.setWeight(weight);

        return classX;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
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
