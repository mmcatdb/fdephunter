package de.uni.passau.server.repository;

import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NegativeExampleRepository extends ReactiveNeo4jRepository<NegativeExampleNode, String> {

    @Query("""
        MATCH (example:NegativeExample)
        WHERE (example.state = 'ANSWERED' OR example.state = 'CONFLICT')
        AND NOT EXISTS (
            (:NegativeExample)-[:HAS_PREVIOUS_ITERATION]->(example)
        )
        RETURN example
        """)
    public Flux<NegativeExampleNode> findAllUnresolved();

    // It's not possible (at least in some simple way) to return the last example as a node. We would get the following error:
    //      org.springframework.data.mapping.mappingexception: More than one matching node in the record
    // There might be a solution with custom mapper - however the price is too high for now.
    // https://stackoverflow.com/questions/75039979/custom-query-projections-with-spring-data-neo4j-two-related-nodes-including-the
    public static record NegativeExampleNodeGroup(
        NegativeExampleNode example,
        @Nullable String lastExampleId
    ) {}

    @Query("""
        MATCH (:Workflow { id: $workflowId })-[:HAS_JOB]->(:DiscoveryJob)-[:HAS_RESULT]->(:DiscoveryResult)-[:HAS_CLASS]->(:Class)-[:HAS_NEGATIVE_EXAMPLE]->(example:NegativeExample)
        WHERE NOT EXISTS (
            (:Assignment)-[:BELONGS_TO_EXAMPLE]->(example)
        )
        OPTIONAL MATCH (example)-[:HAS_PREVIOUS_ITERATION]->(lastExample:NegativeExample)
        RETURN example, lastExample.id as lastExampleId
        """)
    public Flux<NegativeExampleNodeGroup> findUnassignedExamplesForWorkflow(@Param("workflowId") String workflowId);

    // WARN: We need to be careful about this method!
    @Query("""
        MATCH (:Assignment { id: $assignmentId })-[:BELONGS_TO_EXAMPLE]->(nex:NegativeExample)
        WHERE NOT EXISTS (
            (:Assignment { verdict: 'NEW' })-[:BELONGS_TO_EXAMPLE]->(nex)
        )
        SET nex.state = 'ANSWERED'
        RETURN nex
        """)
    public Mono<NegativeExampleNode> updateState(@Param("assignmentId") String assignmentId);

    @Query("""
        MATCH (nex:NegativeExample { id: $exampleId })
        SET nex.state = $state
        RETURN nex
        """)
    public Mono<NegativeExampleNode> saveState(@Param("exampleId") String exampleId, @Param("state") NegativeExampleState state);

    @Query("""
        MATCH (previous:NegativeExample { id: $previousId }), (next:NegativeExample { id: $nextId })
        CREATE (next)-[:HAS_PREVIOUS_ITERATION]->(previous)
        RETURN next
        """)
    public Mono<NegativeExampleNode> saveHasPrevious(@Param("previousId") String previousId, @Param("nextId") String nextId);

}
