package de.uni.passau.server.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.JobResultNode;
import de.uni.passau.server.model.NegativeExampleNode;

public interface AssignmentRepository extends Neo4jRepository<AssignmentNode, String> {

    @Query("""
        MATCH (assignment:Assignment { id: $assignmentId }), (negativeExample:NegativeExample { id: $exampleId })
        CREATE (assignment)-[:BELONGS_TO_EXAMPLE]->(negativeExample)
        RETURN assignment, negativeExample
        """)
    public NegativeExampleNode saveBelongsToExample(@Param("assignmentId") String assignmentId, @Param("exampleId") String exampleId);

    @Query("""
        MATCH (a:Assignment { id: $assignmentId })-[:IN_WORKFLOW]->(:Workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
        RETURN dataset.name
        """)
    public String getDatasetName(@Param("assignmentId") String assignmentId);

    /**
     * Returns the assignment related to the example that is identified by the exampleId.
     */
    @Query("""
        MATCH (assignment:Assignment)-[:BELONGS_TO_EXAMPLE]->(:NegativeExample { id: $exampleId })
        RETURN assignment
        """)
    public Optional<AssignmentNode> findBelongsToExample(@Param("exampleId") String exampleId);

    public static record AssignmentNodeGroup(
        AssignmentNode assignment,
        NegativeExampleNode example,
        JobResultNode result
    ) {}

    @Query("""
        MATCH (assignment:Assignment { id: $assignmentId })-[:BELONGS_TO_EXAMPLE]->(example:NegativeExample)
        MATCH (result:JobResult)-[:HAS_CLASS]->(:Class)-[:HAS_NEGATIVE_EXAMPLE]->(example)
        RETURN assignment, example, result
        """)
    public AssignmentNodeGroup findGroupById(@Param("assignmentId") String assignmentId);

}
