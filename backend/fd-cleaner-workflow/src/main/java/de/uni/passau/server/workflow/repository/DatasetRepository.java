/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.server.workflow.model.DatasetNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
public interface DatasetRepository extends ReactiveNeo4jRepository<DatasetNode, Long> {

    @Query("MATCH(d:Dataset {name: $name}) RETURN d")
    public Mono<DatasetNode> getDatasetByName(@Param("name") String name);

}
