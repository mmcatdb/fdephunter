package de.uni.passau.server.repository;

import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.JobResultNode;

public interface JobResultRepository extends Neo4jRepository<JobResultNode, String> {

    @Query("""
        MATCH (job:DiscoveryJob)<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        WITH MAX(job.iteration) AS maxIteration
        MATCH (result:JobResult)<-[:HAS_RESULT]-(job:DiscoveryJob { iteration: maxIteration })<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        RETURN result
        """)
    public JobResultNode getLastResultByWorkflowId(@Param("workflowId") UUID workflowId);

    @Query("""
        MATCH (job:DiscoveryJob { id: $jobId })
        CREATE (job)-[:HAS_RESULT]->(result:JobResult)
        SET result.id = $resultId, result.payload = $payload
        RETURN result
        """)
    public JobResultNode createResult(@Param("jobId") String jobId, @Param("resultId") String resultId, @Param("payload") String payload);

    @Query("""
        MATCH (:Workflow { id: $workflowId })-[:HAS_JOB]->(:DiscoveryJob { iteration: $iteration })-[:HAS_RESULT]->(result:JobResult)
        RETURN result
        """)
    public JobResultNode findByWorkflowIdAndIteration(@Param("workflowId") UUID workflowId, @Param("iteration") int iteration);

    @Query("""
        MATCH (result:JobResult { id: $resultId }), (class:Class { id: $classId })
        CREATE (result)-[:HAS_CLASS]->(class)
        RETURN result
        """)
    public JobResultNode saveHasClass(@Param("resultId") String resultId, @Param("classId") String classId);

}
