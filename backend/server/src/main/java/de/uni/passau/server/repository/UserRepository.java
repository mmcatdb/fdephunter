package de.uni.passau.server.repository;

import de.uni.passau.server.model.UserNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
