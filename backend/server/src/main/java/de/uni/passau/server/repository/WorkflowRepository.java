package de.uni.passau.server.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;

public interface WorkflowRepository extends Neo4jRepository<WorkflowNode, String> {

    @Query("""
        MATCH (workflow:Workflow { id: $workflowId }), (dataset:Dataset { name: $name })
        CREATE (workflow)-[:HAS_ASSIGNED_DATASET]->(dataset)
        RETURN workflow
        """)
    WorkflowNode saveHasAssignedDataset(@Param("workflowId") String workflowId, @Param("name") String name);

    @Query("""
        MATCH (workflow:Workflow { id: $workflowId }), (job:DiscoveryJob { id: $jobId })
        CREATE (workflow)-[:HAS_JOB]->(job)
        RETURN workflow
        """)
    public WorkflowNode saveHasJob(@Param("workflowId") String workflowId, @Param("jobId") String jobId);

    @Query("MATCH (n) DETACH DELETE n")
    public void purgeDatabase();

    @Query("MATCH (workflow:Workflow { id: $workflowId }) SET workflow.state = $state RETURN workflow")
    public WorkflowNode setState(@Param("workflowId") String workflowId, @Param("state") WorkflowState state);

    @Query("MATCH (workflow:Workflow { id: $workflowId }) SET workflow.iteration = $iteration RETURN workflow")
    public WorkflowNode setIteration(@Param("workflowId") String workflowId, @Param("iteration") int iteration);

    // TODO why this fetches only one dataset while the owner can assign multiple datasets to the workflow?
    @Query("""
        MATCH (w:Workflow {id: $workflowId})-[:HAS_ASSIGNED_DATASET]->(d:Dataset)
        RETURN d.name
        LIMIT 1
        """)
    public String getDatasetName(@Param("workflowId") String workflowId);

}
