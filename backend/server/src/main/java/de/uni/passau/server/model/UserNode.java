package de.uni.passau.server.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("User")
public class UserNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String firstName;

    @Property
    private String lastName;

    @Property
    private String email;

    @Relationship(type = "HAS_ROLE", direction = Relationship.Direction.OUTGOING)
    private List<RoleNode> roles = new ArrayList<>();

    // @Relationship(type = "IS_OWNER", direction = Relationship.Direction.OUTGOING)
    // private List<WorkflowNode> ownerList = new ArrayList<>();

    // @Relationship(type = "IS_EXPERT", direction = Relationship.Direction.OUTGOING)
    // private List<WorkflowNode> expertList = new ArrayList<>();

    public static UserNode createNew(String firstName, String lastName, String email, List<RoleNode> roles) {
        final var user = new UserNode();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);

        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleNode> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleNode> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserNode{");
        sb.append("id=").append(id);
        sb.append(", firstName=").append(firstName);
        sb.append(", lastName=").append(lastName);
        sb.append(", email=").append(email);
        // sb.append(", roles=").append(roles);
        sb.append('}');
        return sb.toString();
    }

}
