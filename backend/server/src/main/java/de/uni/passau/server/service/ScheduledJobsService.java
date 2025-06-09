package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.approach.FDGraphBuilder;
import de.uni.passau.core.approach.FDInit;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.core.nex.Decision;
import de.uni.passau.core.nex.NegativeExample;
import de.uni.passau.core.nex.NegativeExampleBuilder;
import de.uni.passau.core.nex.NegativeExampleUpdater;
import de.uni.passau.server.approach.HyFDAlgorithm;
import de.uni.passau.server.approach.OurApproachAlgorithm;
import de.uni.passau.server.crowdsourcing.CrowdSourcingDummyAlgorithm;
import de.uni.passau.server.crowdsourcing.serverdto.Assignment;
import de.uni.passau.server.crowdsourcing.serverdto.ExpertUser;
import de.uni.passau.server.model.AssignmentNode.ExpertVerdict;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;
import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.ExpertRepository.ExpertNodeGroup;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ScheduledJobsService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ScheduledJobsService.class);

    // private AtomicBoolean LOCKED = new AtomicBoolean(Boolean.FALSE);
    @Autowired
    private DiscoveryJobService discoveryJobService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private NegativeExampleService negativeExampleService;

    @Autowired
    private ExpertService expertService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ConflictService conflictService;

    private static final NegativeExampleUpdater NEGATIVE_EXAMPLE_UPDATER = new NegativeExampleUpdater();    // TODO: Change to @Service

    public void executeUpdateNegativeExampleJobs() {
        LOGGER.info("EXECUTING NEGATIVE EXAMPLE JOB");

        negativeExampleService.getAllUnresolvedExamples().flatMap(example ->
            // fetch all assignments that belong to the same
            assignmentService.findAllBelongsToExample(example.id).collectList().flatMap(assignments -> {
                if (
                    // there is no assignment to negative example -> exit
                    assignments.isEmpty()
                    // at least one domain expert has not answered yet -> exit
                    || assignments.stream().anyMatch(assignment -> assignment.getVerdict().equals(ExpertVerdict.NEW))
                )
                    return Mono.empty();

                // check whether negative example is answered by all expert users:
                int[] verdictsCounter = new int[ExpertVerdict.values().length];
                for (var assignment : assignments)
                    verdictsCounter[assignment.getVerdict().ordinal()] += 1;    // increment

                // We have collected all verdicts, hence we need to check the majority verdict:
                // all verdicts or at least 75% of them are accept -> accept example
                int quorum = (int) Math.ceil(assignments.size() * 0.75);    // quorum >= 75%
                if (verdictsCounter[ExpertVerdict.ACCEPTED.ordinal()] >= quorum) {
                    LOGGER.info("ACCEPTED");
                    return negativeExampleService.changeExampleState(example.id, NegativeExampleState.ACCEPTED);
                }

                final var decisions = new ArrayList<Decision>();
                try {
                    for (var assignment : assignments)
                        decisions.add(Decision.jsonReader.readValue(assignment.getDecision()));
                }
                catch (JsonProcessingException ex) {
                    LOGGER.error("EXCEPTION THROWN TODO", ex);
                    return Mono.empty();    // ERROR: BUG
                }

                // the example has not been accepted yet -> check reasons
                if (conflictService.hasColumnConflicts(decisions)) {
                    LOGGER.info("HAS CONFLICTS");
                    return negativeExampleService.changeExampleState(example.id, NegativeExampleState.CONFLICT);
                }

                // there are no conflicts -> it is simple, we have to update negative example
                // as long as there are no conflicts in columns and we know that there is at least 1 assignment, this is safe
                final var updatedExample = NEGATIVE_EXAMPLE_UPDATER.updateNegativeExample(example, decisions.get(0));

                return classRepository
                    .findHasNegativeExample(example.id)
                    .flatMap(classNode -> negativeExampleService.createExample(classNode.getId(), updatedExample));
            })
        ).subscribe(x -> LOGGER.info("NEGATIVE EXAMPLE JOB FINISHED"));
    }

    public void executeAssignJobs() {
        LOGGER.info("EXECUTING ASSIGN JOB");

        workflowService.getAllWorkflows().flatMap(workflow -> {
            Flux<NegativeExample> unassignedNegativeExamples = negativeExampleService.fetchUnassignedExamples(workflow.getId());
            Flux<ExpertNodeGroup> idleExperts = expertService.findAllIdleInWorkflow(workflow.getId());
            return Mono.zip(unassignedNegativeExamples.collectList(), idleExperts.collectList());
        }).flatMap(tuple -> {
            final List<ExpertUser> idleExperts = tuple.getT2().stream().map(expert -> new ExpertUser(expert.expert().getId())).toList();
            final List<NegativeExample> unassignedNegativeExamples = tuple.getT1();

            //TODO: DISTRIBUTE WORK HERE!
            final var algorithm = new CrowdSourcingDummyAlgorithm();
            final List<Assignment> assignments = algorithm.makeAssignment(idleExperts, unassignedNegativeExamples);

            assignments.forEach(assignment -> {
                LOGGER.info("ASSIGNMENT: {}", assignment);

                assignmentService.createAssignment(assignment.expert.id, assignment.negativeExample.id).subscribe(value -> {
                    LOGGER.info("NEGATIVE EXAMPLE HAS BEEN ASSIGNED: {}", value);
                });

                // AND CHANGE STATE OF USER FROM IDLE TO DIFFERENT!
            });

            return Mono.empty();
        }).subscribe(x -> LOGGER.info("ASSIGN JOB FINISHED"));
    }

    public void executeDiscoveryJobs() {
        LOGGER.info("EXECUTING (RE)DISCOVERY JOB");
        discoveryJobService.findAllJobGroupsByState(DiscoveryJobState.WAITING).flatMap(group -> {
            Mono<List<NegativeExampleNode>> negativeExamplesForResult = discoveryJobService.setState(group.job(), DiscoveryJobState.RUNNING).flatMap(value -> {

                LOGGER.info("Functional dependency discovery job {} has started.", value.getId());
                final var dataset = datasetService.getLoadedDataset(group.dataset());

                // execute functional dependency discovery
                final List<FDInit> result = executeDiscoveryByApproach(dataset, group.approach().getName());
                final WeightedGraph graph = new FDGraphBuilder().buildGraph(result);

                return discoveryJobService.saveResult(group.job().getId(), graph).flatMap(storedResult -> {
                    LOGGER.info("Stored result: {}", storedResult);

                    Flux<Vertex> dependencyClasses = Flux.fromIterable(graph.__getRankedVertices());
                    LOGGER.info("RANKED VERTICES: " + graph.__getRankedVertices());

                    Flux<NegativeExampleNode> fluxClasses = dependencyClasses.flatMap(dependencyClass -> {
                        LOGGER.info("Iterating class {}", dependencyClass);
                        return negativeExampleService.createClass(storedResult.getId(), dependencyClass)
                            .flatMap(classNode -> {
                                LOGGER.info("Class created: {}", classNode);
                                final var builder = new NegativeExampleBuilder(dataset);
                                final var negativeExample = builder.createNew(dependencyClass, 5);    // WARN: TEMPORARY SETTING 5! THE UNDERLYING METHOD MUST BE BETTER IMPLEMENTED
                                return negativeExampleService.createExample(classNode.getId(), negativeExample);
                            });

                    });
                    return fluxClasses.collectList();

                });
            });

            return Mono.zip(Mono.just(group), negativeExamplesForResult);
        }).flatMap(tuple -> {
            var group = tuple.getT1();
            var neX = tuple.getT2();

            LOGGER.info("NEGATIVE SAMPLES: {}", neX);

            Mono<DiscoveryJobNode> updatedJob = discoveryJobService.setState(group.job(), DiscoveryJobState.FINISHED)
                .doOnNext(value -> LOGGER.info("Job {} has finished.", value.getId()));

            Mono<WorkflowNode> updatedWorkflow = workflowService.setState(group.workflow().getId(), WorkflowState.NEGATIVE_EXAMPLES)
                .doOnNext(value -> LOGGER.info("Workflow uuid={} state was updated to {}", value.getId(), value.getState()));

            Mono<WorkflowNode> updatedWorkflow2 = workflowService.setIteration(group.workflow().getId(), group.job().getIteration())
                .doOnNext(value -> LOGGER.info("Workflow uuid={} iteration was updated to {}", value.getId(), value.getIteration()));

            return Mono.when(updatedJob, updatedWorkflow, updatedWorkflow2);
            // return Mono.empty();
        }).subscribe(x -> LOGGER.info("(RE)DISCOVERY JOB FINISHED"));
    }

    private List<FDInit> executeDiscoveryByApproach(Dataset dataset, ApproachName name) {
        switch (name) {
            case HyFD: {
                final var algorithm = new HyFDAlgorithm();
                return algorithm.execute(dataset.getHeader(), dataset.getRows());
            }
            case DepMiner: {
                final var algorithm = new OurApproachAlgorithm();
                return algorithm.execute(dataset.getHeader(), dataset.getRows());
            }
            default:
                throw new UnsupportedOperationException("Not supported approach: " + name);
        }
    }

}
