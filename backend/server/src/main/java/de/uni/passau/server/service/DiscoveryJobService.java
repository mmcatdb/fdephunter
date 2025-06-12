package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.JobResultNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private ClassRepository classRepository;

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

    public DiscoveryJobNode createDiscoveryJob(String workflowId, ApproachName approachName, String description, String dataset) {
        return createJob(workflowId, approachName, description, dataset, WorkflowState.INITIAL_FD_DISCOVERY);
    }

    public DiscoveryJobNode createRediscoveryJob(String workflowId, ApproachName approachName, String description) {
        return createJob(workflowId, approachName, description, null, WorkflowState.JOB_WAITING);
    }

    /** @param dataset Nullable only for rediscovery job. */
    private DiscoveryJobNode createJob(String workflowId, ApproachName approachName, String description, @Nullable String dataset, WorkflowState newState) {
        var workflow = workflowRepository.findById(workflowId).get();
        var job = DiscoveryJobNode.createNew(description, workflow.iteration + 1, approachName);

        job = discoveryJobRepository.save(job);

        workflowRepository.saveHasJob(workflowId, job.getId());
        workflowRepository.saveHasAssignedDataset(workflowId, dataset);

        workflow.state = newState;
        workflow = workflowRepository.save(workflow);

        return job;
    }

    public boolean canCreateRediscoveryJob(String workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();

        // If the workflow isn't in the correct state ...
        if (workflow.state != WorkflowState.NEGATIVE_EXAMPLES)
            return false;

        // If there still are unresolved classes ...
        final var classGroups = classRepository.findAllGroupsByWorkflowId(workflowId);
        return classGroups.stream().allMatch(c -> c.lastExample() != null && c.lastExample().state == NegativeExampleState.ACCEPTED);
    }

}
