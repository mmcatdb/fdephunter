/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.server.workflow.model.RoleNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
public interface RoleRepository extends ReactiveNeo4jRepository<RoleNode, String> {

    @Query("MATCH (r:Role) WHERE r.value = 'EXPERT' RETURN r")
    public Mono<RoleNode> getExpert();
    
    @Query("MATCH (r:Role) WHERE r.value = 'OWNER' RETURN r")
    public Mono<RoleNode> getOwner();

}
