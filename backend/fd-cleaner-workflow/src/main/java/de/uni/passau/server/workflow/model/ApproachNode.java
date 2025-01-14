/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import de.uni.passau.core.approach.ApproachMetadata;
import de.uni.passau.core.approach.AbstractApproach.ApproachName;

/**
 *
 * @author pavel.koupil
 */
@Node("Approach")
public class ApproachNode {

    @Id
    private ApproachName name;

    @Property
    private String label;

    @Property
    private String author;

    public static ApproachNode fromMetadata(ApproachMetadata metadata) {
        final var node = new ApproachNode();
        node.setName(metadata.getName());
        node.setLabel(metadata.getLabel());
        node.setAuthor(metadata.getAuthor());

        return node;
    }

    public ApproachName getName() {
        return name;
    }

    public void setName(ApproachName name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApproachNode{");
        sb.append("name=").append(name);
        sb.append(", label=").append(label);
        sb.append(", author=").append(author);
        sb.append('}');
        return sb.toString();
    }

}
