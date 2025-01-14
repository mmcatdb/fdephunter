/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.server.workflow.model.UserNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
public interface UserRepository extends ReactiveNeo4jRepository<UserNode, String> {

    @Query("""
        MATCH (user:User { id: $userId })-[IS_EXPERT]->(Expert)-[IN_WORKFLOW]->(Workflow { id: $workflowId })
        RETURN count(user) > 0
        """)
    public Mono<Boolean> isUserInWorkflow(@Param("userId") String userId, @Param("workflowId") String workflowId);

    @Query("MATCH (user:User)-[:HAS_ROLE]->(:Role { value: 'EXPERT' }) RETURN user")
    public Flux<UserNode> findAllExpertUsers();

    @Query("""
        MATCH (user:User { id: $userId }), (workflow:Workflow { id: $workflowId })
        CREATE (user)-[isOwner:IS_OWNER]->(workflow)
        RETURN user
        """)
    public Mono<UserNode> saveIsOwner(@Param("userId") String userId, @Param("workflowId") String workflowId);

    @Query("""
        MATCH (user:User { id: $userId }), (expert:Expert { id: $expertId })
        CREATE (user)-[:IS_EXPERT]->(expert)
        RETURN user
        """)
    public Mono<UserNode> saveIsExpert(@Param("userId") String userId, @Param("expertId") String expertId);

}
