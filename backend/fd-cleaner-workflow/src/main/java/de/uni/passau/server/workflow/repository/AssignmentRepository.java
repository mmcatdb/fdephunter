/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.server.workflow.model.AssignmentNode;
import de.uni.passau.server.workflow.model.AssignmentNode.ExpertVerdict;
import de.uni.passau.server.workflow.model.DiscoveryResultNode;
import de.uni.passau.server.workflow.model.ExpertNode;
import de.uni.passau.server.workflow.model.NegativeExampleNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
public interface AssignmentRepository extends ReactiveNeo4jRepository<AssignmentNode, String> {

    @Query("""
        MATCH (assignment:Assignment { id: $assignmentId }), (negativeExample:NegativeExample { id: $exampleId })
        CREATE (assignment)-[:BELONGS_TO_EXAMPLE]->(negativeExample)
        RETURN assignment, negativeExample
        """)
    public Mono<NegativeExampleNode> saveBelongsToExample(@Param("assignmentId") String assignmentId, @Param("exampleId") String exampleId);

    @Query("""
        MATCH (assignment:Assignment { id: $assignmentId })
        SET assignment.verdict = $verdict, assignment.decision = $decision
        RETURN assignment
        """)
    public Mono<AssignmentNode> evaluateAssignment(@Param("assignmentId") String assignmentId, @Param("verdict") ExpertVerdict verdict, @Param("decision") String decision);

    @Query("""
        MATCH (a:Assignment { id: $assignmentId })<-[:HAS_ASSIGNMENT]-(:Expert)-[:IN_WORKFLOW]->(:Workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
        RETURN dataset.name
        """)
    public Mono<String> getDatasetName(@Param("assignmentId") String assignmentId);

    /**
     * Returns all assignments related to the example that is identified by the exampleId.
     */
    @Query("""
        MATCH (assignment:Assignment)-[:BELONGS_TO_EXAMPLE]->(:NegativeExample { id: $exampleId })
        RETURN assignment
        """)
    public Flux<AssignmentNode> findAllBelongsToExample(@Param("exampleId") String exampleId);

    /**
     * Returns the current active assignment related to the example that is identified by the exampleId and belongs to the expert identified by the expertId.
     */
    @Query("""
        MATCH (:Expert { id: $expertId })-[:HAS_ASSIGNMENT]->(assignment:Assignment)-[:BELONGS_TO_EXAMPLE]->(:NegativeExample { id: $exampleId })
        WHERE assignment.verdict = 'NEW'
        RETURN assignment
        """)
    public Mono<AssignmentNode> findNewFromExpertBelongsToExample(@Param("expertId") String expertId, @Param("exampleId") String exampleId);

    /**
     * Ordering from the most recent one to the last.
     */
    @Query("""
        MATCH (:Expert { id: $expertId })-[:HAS_ASSIGNMENT]->(assignment:Assignment)
        WHERE assignment.verdict <> 'NEW'
        RETURN assignment
        ORDER BY assignment.createdAt DESC
        """)
    public Flux<AssignmentNode> findAllAnsweredFromExpert(@Param("expertId") String expertId);

    public static record AssignmentNodeGroup(
        AssignmentNode assignment,
        ExpertNode expert,
        NegativeExampleNode example,
        DiscoveryResultNode result
    ) {}

    @Query("""
        MATCH (expert:Expert)-[:HAS_ASSIGNMENT]->(assignment:Assignment { id: $assignmentId })-[:BELONGS_TO_EXAMPLE]->(example:NegativeExample)
        MATCH (result:DiscoveryResult)-[:HAS_CLASS]->(:Class)-[:HAS_NEGATIVE_EXAMPLE]->(example)
        RETURN assignment, expert, example, result
        """)
    public Mono<AssignmentNodeGroup> findGroupById(@Param("assignmentId") String assignmentId);

}
