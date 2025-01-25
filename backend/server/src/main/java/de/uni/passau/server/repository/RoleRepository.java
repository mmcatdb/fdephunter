package de.uni.passau.server.repository;

import de.uni.passau.server.model.RoleNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveNeo4jRepository<RoleNode, String> {

    @Query("MATCH (r:Role) WHERE r.value = 'EXPERT' RETURN r")
    public Mono<RoleNode> getExpert();

    @Query("MATCH (r:Role) WHERE r.value = 'OWNER' RETURN r")
    public Mono<RoleNode> getOwner();

}
