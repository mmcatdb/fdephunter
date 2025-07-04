package de.uni.passau.server.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Document("job")
public class JobEntity {

    @Id @JsonProperty("id")
    private UUID _id;

    public UUID id() {
        return _id;
    }

    public UUID workflowId;

    /** For ordering jobs in the workflow. */
    public Integer index;

    public String description;

    public JobState state;

    public @Nullable Date startedAt;

    public @Nullable Date finishedAt;

    public JobPayload payload;

    public static JobEntity create(UUID workflowId, int index, String description, JobPayload payload) {
        final var job = new JobEntity();

        job._id = UUID.randomUUID();
        job.workflowId = workflowId;
        job.index = index;
        job.description = description;
        job.state = JobState.WAITING;
        job.startedAt = null;
        job.finishedAt = null;
        job.payload = payload;

        return job;
    }

    public enum JobState {
        WAITING,
        RUNNING,
        FINISHED,
        FAILED,
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DiscoveryJobPayload.class, name = "discovery"),
        @JsonSubTypes.Type(value = IterationJobPayload.class, name = "iteration"),
    })
    public interface JobPayload extends Serializable {}

    public record DiscoveryJobPayload(
        UUID datasetId
    ) implements JobPayload {}

    public record IterationJobPayload() implements JobPayload {}

}
