package de.uni.passau.server.repository;

import de.uni.passau.server.model.ClassNode;
import de.uni.passau.server.model.NegativeExampleNode;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClassRepository extends ReactiveNeo4jRepository<ClassNode, String> {

    // find class of related negative example
    @Query("""
        MATCH (class:Class)-[:HAS_NEGATIVE_EXAMPLE]->(:NegativeExample { id: $exampleId })
        RETURN class
        """)
    public Mono<ClassNode> findHasNegativeExample(@Param("exampleId") String exampleId);

    @Query("""
        MATCH (class:Class { id: $classId }), (example:NegativeExample { id: $exampleId })
        CREATE (class)-[:HAS_NEGATIVE_EXAMPLE]->(example)
        RETURN class
        """)
    public Mono<ClassNode> saveHasNegativeExample(@Param("classId") String classId, @Param("exampleId") String exampleId);

    public static record ClassNodeGroup(
        ClassNode classX,
        Integer iteration,
        @Nullable NegativeExampleNode lastExample
    ) {
        public ClassNodeGroup(ClassNode classX, Integer iteration, @Nullable NegativeExampleNode lastExample) {
            this.classX = classX;
            this.iteration = iteration;
            this.lastExample = (lastExample == null || lastExample.getId() == null) ? null : lastExample;
        }
    }

    @Query("""
        MATCH (workflow:Workflow { id: $workflowId })-[:HAS_JOB]->(job:DiscoveryJob)-[:HAS_RESULT]->(:DiscoveryResult)-[:HAS_CLASS]->(class:Class)
        WHERE workflow.iteration = job.iteration
        OPTIONAL MATCH (class)-[:HAS_NEGATIVE_EXAMPLE]->(example:NegativeExample)
        OPTIONAL MATCH (class)-[:HAS_NEGATIVE_EXAMPLE]->(lastExample:NegativeExample)
        WHERE NOT EXISTS (
            (:NegativeExample)-[:HAS_PREVIOUS_ITERATION]->(lastExample)
        )
        RETURN class as classX, count(example) as iteration, CASE WHEN lastExample IS NULL THEN {} ELSE lastExample END AS lastExample
        """)
    public Flux<ClassNodeGroup> findAllGroupsByWorkflowId(@Param("workflowId") String workflowId);

}
