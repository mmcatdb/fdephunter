package de.uni.passau.server.repository;

import de.uni.passau.server.model.DatasetNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface DatasetRepository extends ReactiveNeo4jRepository<DatasetNode, Long> {

    @Query("MATCH(d:Dataset {name: $name}) RETURN d")
    public Mono<DatasetNode> getDatasetByName(@Param("name") String name);

}
