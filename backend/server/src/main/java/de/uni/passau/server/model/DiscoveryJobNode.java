package de.uni.passau.server.model;

import java.util.Date;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

@Node("DiscoveryJob")
public class DiscoveryJobNode {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String description;

    @Property
    public DiscoveryJobState state;

    @Property
    public Integer iteration;

    @Property
    public Date startedAt;

    @Property
    public ApproachName approach;

    public static DiscoveryJobNode createNew(String description, int iteration, ApproachName approach) {
        final var job = new DiscoveryJobNode();
        job.description = description;
        job.state = DiscoveryJobState.WAITING;
        job.iteration = iteration;
        job.startedAt = new Date();
        job.approach = approach;

        return job;
    }

    public enum DiscoveryJobState {
        WAITING,
        RUNNING,
        FINISHED,
        FAILED,
    }

}
