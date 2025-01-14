/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.server.workflow.model.WorkflowNode;
import de.uni.passau.server.workflow.model.WorkflowNode.WorkflowState;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
public interface WorkflowRepository extends ReactiveNeo4jRepository<WorkflowNode, String> {

    @Query("""
        MATCH (workflow:Workflow { id: $workflowId }), (dataset:Dataset { name: $name })
        CREATE (workflow)-[:HAS_ASSIGNED_DATASET]->(dataset)
        RETURN workflow
        """)
    Mono<WorkflowNode> saveHasAssignedDataset(@Param("workflowId") String workflowId, @Param("name") String name);

    @Query("""
        MATCH (workflow:Workflow { id: $workflowId }), (job:DiscoveryJob { id: $jobId })
        CREATE (workflow)-[:HAS_JOB]->(job)
        RETURN workflow
        """)
    public Mono<WorkflowNode> saveHasJob(@Param("workflowId") String workflowId, @Param("jobId") String jobId);

    @Query("MATCH (n) DETACH DELETE n")
    public Mono<Void> purgeDatabase();

    @Query("MATCH (workflow:Workflow { id: $workflowId }) SET workflow.state = $state RETURN workflow")
    public Mono<WorkflowNode> setState(@Param("workflowId") String workflowId, @Param("state") WorkflowState state);

    @Query("MATCH (workflow:Workflow { id: $workflowId }) SET workflow.iteration = $iteration RETURN workflow")
    public Mono<WorkflowNode> setIteration(@Param("workflowId") String workflowId, @Param("iteration") int iteration);

    // TODO why this fetches only one dataset while the owner can assign multiple datasets to the workflow?
    @Query("""
        MATCH (w:Workflow {id: $workflowId})-[:HAS_ASSIGNED_DATASET]->(d:Dataset)
        RETURN d.name
        LIMIT 1
        """)
    public Mono<String> getDatasetName(@Param("workflowId") String workflowId);

}
