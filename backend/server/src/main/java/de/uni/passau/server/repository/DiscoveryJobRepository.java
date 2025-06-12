package de.uni.passau.server.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.DatasetNode;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DiscoveryJobRepository extends ReactiveNeo4jRepository<DiscoveryJobNode, String> {

    @Query("MATCH(job:DiscoveryJob { id: $jobId }) RETURN job")
    public Mono<DiscoveryJobNode> getJobById(@Param("jobId") String id);

    @Query("MATCH (job:DiscoveryJob { id: $jobId }) SET job.state = $state RETURN job")
    public Mono<DiscoveryJobNode> setState(@Param("jobId") String id, @Param("state") DiscoveryJobState state);

    @Query("""
        MATCH (job:DiscoveryJob)<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        WITH MAX(job.iteration) AS maxIteration
        MATCH (job:DiscoveryJob { iteration: maxIteration })<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        RETURN job
        """)
    public Mono<DiscoveryJobNode> getLastDiscoveryByWorkflowId(@Param("workflowId") String workflowId);

    public static record DiscoveryJobNodeGroup(
        DiscoveryJobNode job,
        WorkflowNode workflow,
        DatasetNode dataset
    ) {}

    @Query("""
        MATCH (job:DiscoveryJob { state: $state })<-[:HAS_JOB]-(workflow:Workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
        RETURN job, workflow, dataset
        """)
    public Flux<DiscoveryJobNodeGroup> findAllGroupsByState(@Param("state") DiscoveryJobState state);

}
