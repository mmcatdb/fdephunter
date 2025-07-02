package de.uni.passau.server.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.DatasetNode;

public interface DatasetRepository extends Neo4jRepository<DatasetNode, String> {

    @Query("MATCH(d:Dataset {name: $name}) RETURN d")
    public DatasetNode getDatasetByName(@Param("name") String name);

}
