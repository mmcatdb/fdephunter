package de.uni.passau.server.repository;

import de.uni.passau.server.model.DiscoveryResultNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface DiscoveryResultRepository extends ReactiveNeo4jRepository<DiscoveryResultNode, String> {

    @Query("""
        MATCH (job:DiscoveryJob)<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        WITH MAX(job.iteration) AS maxIteration
        MATCH (result:DiscoveryResult)<-[:HAS_RESULT]-(job:DiscoveryJob { iteration: maxIteration })<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        RETURN result
        """)
    public Mono<DiscoveryResultNode> getLastResultByWorkflowId(@Param("workflowId") String workflowId);

    @Query("""
        MATCH (job:DiscoveryJob { id: $jobId })
        CREATE (job)-[:HAS_RESULT]->(result:DiscoveryResult)
        SET result.id = $resultId, result.payload = $payload
        RETURN result
        """)
    public Mono<DiscoveryResultNode> createResult(@Param("jobId") String jobId, @Param("resultId") String resultId, @Param("payload") String payload);

    @Query("""
        MATCH (:Workflow { id: $workflowId })-[:HAS_JOB]->(:DiscoveryJob { iteration: $iteration })-[:HAS_RESULT]->(result:DiscoveryResult)
        RETURN result
        """)
    public Mono<DiscoveryResultNode> findByWorkflowIdAndIteration(@Param("workflowId") String workflowId, @Param("iteration") int iteration);

    @Query("""
        MATCH (result:DiscoveryResult { id: $resultId }), (class:Class { id: $classId })
        CREATE (result)-[:HAS_CLASS]->(class)
        RETURN result
        """)
    public Mono<DiscoveryResultNode> saveHasClass(@Param("resultId") String resultId, @Param("classId") String classId);

}
