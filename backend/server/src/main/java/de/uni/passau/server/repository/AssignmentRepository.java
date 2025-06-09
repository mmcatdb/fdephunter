package de.uni.passau.server.repository;

import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.AssignmentNode.AssignmentVerdict;
import de.uni.passau.server.model.DiscoveryResultNode;
import de.uni.passau.server.model.NegativeExampleNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<AssignmentNode> evaluateAssignment(@Param("assignmentId") String assignmentId, @Param("verdict") AssignmentVerdict verdict, @Param("decision") String decision);

    @Query("""
        MATCH (a:Assignment { id: $assignmentId })-[:IN_WORKFLOW]->(:Workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
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

    public static record AssignmentNodeGroup(
        AssignmentNode assignment,
        NegativeExampleNode example,
        DiscoveryResultNode result
    ) {}

    @Query("""
        MATCH (assignment:Assignment { id: $assignmentId })-[:BELONGS_TO_EXAMPLE]->(example:NegativeExample)
        MATCH (result:DiscoveryResult)-[:HAS_CLASS]->(:Class)-[:HAS_NEGATIVE_EXAMPLE]->(example)
        RETURN assignment, example, result
        """)
    public Mono<AssignmentNodeGroup> findGroupById(@Param("assignmentId") String assignmentId);

}
