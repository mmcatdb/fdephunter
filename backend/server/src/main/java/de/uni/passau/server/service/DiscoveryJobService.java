package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.JobResultNode;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryJobService.class);

    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;

    @Autowired
    private JobResultRepository jobResultRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ObjectMapper objectMapperJSON;

    public JobResultNode saveResult(String jobId, WeightedGraph result) {
        String payload;
        try {
            payload = objectMapperJSON.writeValueAsString(result);
        }
        catch (JsonProcessingException e) {
            payload = "EXCEPTION - JSON_PROCESSING_EXCEPTION";
            LOGGER.error("FIX THIS ERROR - INVALID SERIALIZATION OF GRAPH");
        }

        final String resultId = jobId + "::" + System.currentTimeMillis();

        return jobResultRepository.createResult(jobId, resultId, payload);
    }

    public DiscoveryJobNode createDiscoveryJob(UUID workflowId, ApproachName approachName, String description, UUID datasetId) {
        return createJob(workflowId, approachName, description, datasetId, WorkflowState.INITIAL_FD_DISCOVERY);
    }

    public DiscoveryJobNode createRediscoveryJob(UUID workflowId, ApproachName approachName, String description) {
        return createJob(workflowId, approachName, description, null, WorkflowState.JOB_WAITING);
    }

    /** @param dataset Nullable only for rediscovery job. */
    private DiscoveryJobNode createJob(UUID workflowId, ApproachName approachName, String description, @Nullable UUID datasetId, WorkflowState newState) {
        var workflow = workflowRepository.findById(workflowId).get();
        var job = DiscoveryJobNode.createNew(description, workflow.iteration + 1, approachName);

        job = discoveryJobRepository.save(job);

        // FIXME Save this to the job instead.
        // workflowRepository.saveHasJob(workflowId, job.getId());
        if (datasetId != null)
            workflow.datasetId = datasetId;

        workflow.state = newState;
        workflow = workflowRepository.save(workflow);

        return job;
    }

}
