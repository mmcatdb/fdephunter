package de.uni.passau.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;

public interface DiscoveryJobRepository extends Neo4jRepository<DiscoveryJobNode, String> {

    @Query("MATCH(job:DiscoveryJob { id: $jobId }) RETURN job")
    public DiscoveryJobNode getJobById(@Param("jobId") String id);

    @Query("MATCH (job:DiscoveryJob { id: $jobId }) SET job.state = $state RETURN job")
    public DiscoveryJobNode setState(@Param("jobId") String id, @Param("state") DiscoveryJobState state);

    @Query("""
        MATCH (job:DiscoveryJob)<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        WITH MAX(job.iteration) AS maxIteration
        MATCH (job:DiscoveryJob { iteration: maxIteration })<-[:HAS_JOB]-(:Workflow { id: $workflowId })
        RETURN job
        """)
    public DiscoveryJobNode getLastDiscoveryByWorkflowId(@Param("workflowId") UUID workflowId);

    public static record DiscoveryJobNodeGroup(
        DiscoveryJobNode job,
        WorkflowEntity workflow,
        DatasetEntity dataset
    ) {}

    @Query("""
        MATCH (job:DiscoveryJob { state: $state })<-[:HAS_JOB]-(workflow:Workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
        RETURN job, workflow, dataset
        """)
    public List<DiscoveryJobNodeGroup> findAllGroupsByState(@Param("state") DiscoveryJobState state);

}
