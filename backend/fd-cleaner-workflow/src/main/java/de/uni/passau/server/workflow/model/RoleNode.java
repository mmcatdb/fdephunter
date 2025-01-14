/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 *
 * @author pavel.koupil
 */
@Node("Role")
public class RoleNode {

    public enum RoleType {
        EXPERT,
        OWNER,
    }

    @Id
    private String id;

    @Property
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static RoleNode createNew(RoleType type) {
        final var role = new RoleNode();
        role.setId(type.toString());
        role.setValue(type.toString());

        return role;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RoleNode{");
        sb.append("id=").append(id);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

}
