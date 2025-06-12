package de.uni.passau.server.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.JobResultNode;
import reactor.core.publisher.Mono;

public interface JobResultRepository extends ReactiveNeo4jRepository<JobResultNode, String> {

    @Query("""
        MATCH (job:DiscoveryJob)<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        WITH MAX(job.iteration) AS maxIteration
        MATCH (result:JobResult)<-[:HAS_RESULT]-(job:DiscoveryJob { iteration: maxIteration })<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        RETURN result
        """)
    public Mono<JobResultNode> getLastResultByWorkflowId(@Param("workflowId") String workflowId);

    @Query("""
        MATCH (job:DiscoveryJob { id: $jobId })
        CREATE (job)-[:HAS_RESULT]->(result:JobResult)
        SET result.id = $resultId, result.payload = $payload
        RETURN result
        """)
    public Mono<JobResultNode> createResult(@Param("jobId") String jobId, @Param("resultId") String resultId, @Param("payload") String payload);

    @Query("""
        MATCH (:Workflow { id: $workflowId })-[:HAS_JOB]->(:DiscoveryJob { iteration: $iteration })-[:HAS_RESULT]->(result:JobResult)
        RETURN result
        """)
    public Mono<JobResultNode> findByWorkflowIdAndIteration(@Param("workflowId") String workflowId, @Param("iteration") int iteration);

    @Query("""
        MATCH (result:JobResult { id: $resultId }), (class:Class { id: $classId })
        CREATE (result)-[:HAS_CLASS]->(class)
        RETURN result
        """)
    public Mono<JobResultNode> saveHasClass(@Param("resultId") String resultId, @Param("classId") String classId);

}
