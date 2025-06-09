package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;
import de.uni.passau.server.model.DiscoveryResultNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.DiscoveryJobRepository.DiscoveryJobNodeGroup;
import de.uni.passau.server.repository.DiscoveryResultRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DiscoveryJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryJobService.class);

    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;

    @Autowired
    private DiscoveryResultRepository discoveryResultRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ObjectMapper objectMapperJSON;

    public Mono<DiscoveryJobNode> getJobById(String jobId) {
        return discoveryJobRepository.getJobById(jobId);
    }

    public Flux<DiscoveryJobNodeGroup> findAllJobGroupsByState(DiscoveryJobState state) {
        return discoveryJobRepository.findAllGroupsByState(state);
    }

    public Mono<DiscoveryResultNode> saveResult(String jobId, WeightedGraph result) {
        String payload;
        try {
            payload = objectMapperJSON.writeValueAsString(result);
        }
        catch (JsonProcessingException e) {
            payload = "EXCEPTION - JSON_PROCESSING_EXCEPTION";
            LOGGER.error("FIX THIS ERROR - INVALID SERIALIZATION OF GRAPH");
        }

        String resultId = jobId + "::" + System.currentTimeMillis();

        return discoveryResultRepository.createResult(jobId, resultId, payload)
                .onErrorResume(InvalidDataAccessResourceUsageException.class, e -> {
                    // Handle the specific exception for invalid resource usage
                    String errorMessage = "Invalid data access resource usage: " + e.getMessage();
                    return Mono.error(new Exception(errorMessage));
                })
                .onErrorResume(DataAccessException.class, e -> {
                    // Handle other data access exceptions
                    String errorMessage = "Data access exception: " + e.getMessage();
                    return Mono.error(new Exception(errorMessage));
                });
    }

    public Mono<DiscoveryResultNode> getResult(String workflowId, int iteration) {
        return discoveryResultRepository.findByWorkflowIdAndIteration(workflowId, iteration);
        // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public Mono<DiscoveryJobNode> setState(DiscoveryJobNode job, DiscoveryJobState discoveryJobState) {
        return discoveryJobRepository.setState(job.getId(), discoveryJobState);
    }

    public Mono<DiscoveryJobNode> getLastDiscoveryByWorkflowId(String workflowId) {
        return discoveryJobRepository.getLastDiscoveryByWorkflowId(workflowId);
    }

    private Mono<DiscoveryJobNode> createJob(String workflowId, ApproachName approachName, String description, List<String> datasets, WorkflowState newState) {
        return workflowRepository.findById(workflowId).flatMap(workflow -> {
            final var node = DiscoveryJobNode.createNew(description, workflow.getIteration() + 1);
            return discoveryJobRepository.save(node).flatMap(job ->
                workflowRepository.saveHasJob(workflowId, job.getId())
                    .then(workflowRepository.setState(workflowId, newState))
                    .then(
                        Flux.fromIterable(datasets)
                            .flatMap(dataset -> workflowRepository.saveHasAssignedDataset(workflowId, dataset))
                            .collectList()
                    )
                    .then(discoveryJobRepository.saveUtilizesApproach(job.getId(), approachName))
            );
        });
    }

    public Mono<DiscoveryJobNode> createDiscoveryJob(String workflowId, ApproachName approachName, String description, List<String> datasets) {
        return createJob(workflowId, approachName, description, datasets, WorkflowState.INITIAL_FD_DISCOVERY);
    }

    public Mono<DiscoveryJobNode> createRediscoveryJob(String workflowId, ApproachName approachName, String description) {
        return createJob(workflowId, approachName, description, List.of(), WorkflowState.JOB_WAITING);
    }

    public Mono<Boolean> canCreateRediscoveryJob(String workflowId) {
        return workflowRepository.findById(workflowId)
            // If the workflow isn't in the correct state ...
            .flatMap(workflow -> workflow.getState() == WorkflowState.NEGATIVE_EXAMPLES
                ? classRepository.findAllGroupsByWorkflowId(workflowId).collectList().map(classes ->
                    // If there still are unresolved classes ...
                    classes.stream().allMatch(c -> c.lastExample() != null && c.lastExample().getState() == NegativeExampleState.ACCEPTED)
                )
                : Mono.just(false)
            );
    }

}
