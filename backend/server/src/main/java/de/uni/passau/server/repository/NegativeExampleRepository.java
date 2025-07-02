package de.uni.passau.server.repository;

import java.util.List;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;

public interface NegativeExampleRepository extends Neo4jRepository<NegativeExampleNode, String> {

    @Query("""
        MATCH (example:NegativeExample)
        WHERE (example.state = 'ANSWERED')
        AND NOT EXISTS (
            (:NegativeExample)-[:HAS_PREVIOUS_ITERATION]->(example)
        )
        RETURN example
        """)
    public List<NegativeExampleNode> findAllUnresolved();

    // It's not possible (at least in some simple way) to return the last example as a node. We would get the following error:
    //      org.springframework.data.mapping.mappingexception: More than one matching node in the record
    // There might be a solution with custom mapper - however the price is too high for now.
    // https://stackoverflow.com/questions/75039979/custom-query-projections-with-spring-data-neo4j-two-related-nodes-including-the
    public static record NegativeExampleNodeGroup(
        NegativeExampleNode example,
        @Nullable String lastExampleId
    ) {}

    @Query("""
        MATCH (nex:NegativeExample { id: $exampleId })
        SET nex.state = $state
        RETURN nex
        """)
    public NegativeExampleNode saveState(@Param("exampleId") String exampleId, @Param("state") NegativeExampleState state);

    @Query("""
        MATCH (previous:NegativeExample { id: $previousId }), (next:NegativeExample { id: $nextId })
        CREATE (next)-[:HAS_PREVIOUS_ITERATION]->(previous)
        RETURN next
        """)
    public NegativeExampleNode saveHasPrevious(@Param("previousId") String previousId, @Param("nextId") String nextId);

}
