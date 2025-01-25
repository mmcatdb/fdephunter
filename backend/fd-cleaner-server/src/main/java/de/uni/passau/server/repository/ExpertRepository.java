package de.uni.passau.server.repository;

import de.uni.passau.server.model.AssignmentNode;
import de.uni.passau.server.model.ExpertNode;
import de.uni.passau.server.model.ExpertNode.ExpertState;
import de.uni.passau.server.model.UserNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpertRepository extends ReactiveNeo4jRepository<ExpertNode, String> {

    @Query("""
        MATCH (expert:Expert { id: $expertId }), (workflow:Workflow { id: $workflowId })
        CREATE (expert)-[:IN_WORKFLOW]->(workflow)
        RETURN expert
        """)
    public Mono<ExpertNode> saveInWorkflow(@Param("expertId") String expertId, @Param("workflowId") String workflowId);

    @Query("""
        MATCH (expert:Expert { id: $expertId }), (assignment:Assignment { id: $assignmentId })
        CREATE (expert)-[:HAS_ASSIGNMENT]->(assignment)
        RETURN expert
        """)
    public Mono<ExpertNode> saveHasAssignment(@Param("expertId") String expertId, @Param("assignmentId") String assignmentId);

    @Query("""
        MATCH (expert:Expert)-[:HAS_ASSIGNMENT]->(assignment:Assignment { id: $assignmentId })
        MATCH (user:User)-[:IS_EXPERT]->(expert)
        SET expert.state = 'IDLE'
        RETURN expert
        """)
    public Mono<ExpertNode> evaluateAssignment(@Param("assignmentId") String assignmentId);

    public static record ExpertNodeGroup(
        ExpertNode expert,
        UserNode user,
        @Nullable AssignmentNode assignment // Only the current active assignment (i.e., with the NEW verdict) should be here.
    ) {
        public ExpertNodeGroup(ExpertNode expert, UserNode user, @Nullable AssignmentNode assignment) {
            this.expert = expert;
            this.user = user;
            this.assignment = (assignment == null || assignment.getId() == null) ? null : assignment;
        }
    }

    // An idle expert doesn't have any new assignments.
    @Query("""
        MATCH (expert:Expert { state: 'IDLE' })-[:IN_WORKFLOW]->(workflow:Workflow { id: $workflowId })
        MATCH (user:User)-[:IS_EXPERT]->(expert)
        RETURN expert, user, {} as assignment
        """)
    public Flux<ExpertNodeGroup> findAllIdleInWorkflow(@Param("workflowId") String workflowId);

    @Query("""
        MATCH (expert:Expert)-[:IN_WORKFLOW]->(workflow:Workflow { id: $workflowId })
        MATCH (user:User)-[:IS_EXPERT]->(expert)
        OPTIONAL MATCH (expert)-[:HAS_ASSIGNMENT]->(assignment:Assignment)
        WHERE assignment.verdict = 'NEW'
        RETURN expert, user, CASE WHEN assignment IS NULL THEN {} ELSE assignment END AS assignment
        """)
    public Flux<ExpertNodeGroup> findAllInWorkflow(@Param("workflowId") String workflowId);

    @Query("""
        MATCH (user:User)-[:IS_EXPERT]->(expert:Expert { id: $expertId })
        OPTIONAL MATCH (expert)-[:HAS_ASSIGNMENT]->(assignment:Assignment)
        WHERE assignment.verdict = 'NEW'
        RETURN expert, user, CASE WHEN assignment IS NULL THEN {} ELSE assignment END AS assignment
        """)
    public Mono<ExpertNodeGroup> findGroupById(@Param("expertId") String expertId);

    @Query("""
        MATCH (user:User)-[:IS_EXPERT]->(expert:Expert { id: $expertId })
        OPTIONAL MATCH (expert)-[:HAS_ASSIGNMENT]->(assignment:Assignment)
        WHERE assignment.verdict = 'NEW'
        SET expert.state = $state
        RETURN expert, user, CASE WHEN assignment IS NULL THEN {} ELSE assignment END AS assignment
        """)
    public Mono<ExpertNodeGroup> setState(@Param("expertId") String expertId, @Param("state") ExpertState state);

}
