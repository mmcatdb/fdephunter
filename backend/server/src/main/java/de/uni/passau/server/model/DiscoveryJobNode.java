package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("DiscoveryJob")
public class DiscoveryJobNode {

    public enum DiscoveryJobState {
        WAITING,
        RUNNING,
        PENDING,
        FINISHED,
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String description;

    @Property
    private DiscoveryJobState state;

    @Property
    private Integer iteration;

    public static DiscoveryJobNode createNew(String description, int iteration) {
        final var job = new DiscoveryJobNode();
        job.setDescription(description);
        job.setState(DiscoveryJobState.WAITING);
        job.setIteration(iteration);

        return job;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscoveryJobState getState() {
        return state;
    }

    public void setState(DiscoveryJobState state) {
        this.state = state;
    }

    public Integer getIteration() {
        return iteration;
    }

    public void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiscoveryJobNode{");
        sb.append("id=").append(id);
        sb.append(", description=").append(description);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

}
