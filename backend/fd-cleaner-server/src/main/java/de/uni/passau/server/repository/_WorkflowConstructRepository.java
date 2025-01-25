package de.uni.passau.server.repository;

import de.uni.passau.server.model._WorkflowConstruct;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface _WorkflowConstructRepository extends ReactiveNeo4jRepository<_WorkflowConstruct, Long> {

    @Query("""
        MATCH (workflow:Workflow {id: $workflowId})
        with workflow
        OPTIONAL MATCH(workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
        OPTIONAL MATCH(workflow)-[jobIteration:HAS_JOB]->(job:DiscoveryJob)
        OPTIONAL MATCH(job:DiscoveryJob)-[:HAS_RESULT]->(fd:FunctionalDependency)
        OPTIONAL MATCH(fd)-[:HAS_CLASS]->(class:Class)
        OPTIONAL MATCH(class)-[nexEdge:HAS_NEGATIVE_EXAMPLE]->(nex:NegativeExample)
        OPTIONAL MATCH(user:DomainExpert)-[assignment:HAS_ASSIGNMENT]->(class)
        WITH DISTINCT class, workflow, dataset, jobIteration, job, fd, nexEdge, nex, user, assignment
        ORDER BY fd.iteration DESC
        WITH workflow, dataset, jobIteration, job, COLLECT(fd)[0] AS fd, class, nexEdge, nex, user, assignment
        RETURN
            workflow.id AS workflowId,
            workflow.state AS state,
            workflow.iteration AS iteration,
            COLLECT({
                state: job.state,
                description: job.description,
            }) AS jobs,
            COLLECT({
                lhs: fd.lhs,
                rhs: fd.rhs
            }) AS fds,
            COLLECT({
                version: nexEdge.version,
                content: nex.content,
                sampleId: nex.sampleId
            }) AS negativeExamples,
            COLLECT({
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                columns: assignment.columns,
                reason: assignment.reason,
                verdict: assignment.verdict,
                content: nex.content
            }) AS decisions
        """)
    public Mono<_WorkflowConstruct> getWorkflow(@Param("workflowId") String workflowId);

//    MATCH (workflow:Workflow {id: $workflowId})
//OPTIONAL MATCH (workflow)-[:HAS_ASSIGNED_DATASET]->(dataset:Dataset)
//OPTIONAL MATCH (workflow)-[jobIteration:HAS_JOB]->(job:DiscoveryJob)
//OPTIONAL MATCH (workflow)-[:HAS_RESULT]->(fd:FunctionalDependency)
//OPTIONAL MATCH (fd)-[:HAS_CLASS]->(class:Class)
//OPTIONAL MATCH (class)-[nexEdge:HAS_NEGATIVE_EXAMPLE]->(nex:NegativeExample)
//WITH DISTINCT class, workflow, dataset, jobIteration, job, fd,
//     COLLECT(DISTINCT nex)[0] AS highestVersionNegativeExample,
//     MAX(nex.version) AS highestVersion
//ORDER BY fd.iteration DESC
//WITH workflow, dataset, jobIteration, job, COLLECT(fd)[0] AS fd,
//     COLLECT(highestVersionNegativeExample) AS negativeExamples,
//     COLLECT(DISTINCT class) AS classes
//RETURN
//    workflow.id AS workflowId,
//    workflow.state AS state,
//    workflow.iteration AS iteration,
//    COLLECT({ state: job.state,
//              description: job.description }) AS jobs,
//    COLLECT({ lhs: fd.lhs,
//              rhs: fd.rhs }) AS fds,
//    negativeExamples,
//    COLLECT({
//        firstName: classes[0].firstName,
//        lastName: classes[0].lastName,
//        email: classes[0].email,
//        decisions: [
//            {
//                columns: assignment.columns,
//                reason: assignment.reason,
//                verdict: assignment.verdict
//            }
//        ]
//    }) AS decisions


}
