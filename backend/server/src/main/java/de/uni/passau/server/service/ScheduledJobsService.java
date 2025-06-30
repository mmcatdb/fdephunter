package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.approach.FDGraphBuilder;
import de.uni.passau.core.approach.FDInit;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.server.approach.HyFDAlgorithm;
import de.uni.passau.server.approach.OurApproachAlgorithm;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.WorkflowNode;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;
import de.uni.passau.server.model.WorkflowNode.WorkflowState;
import de.uni.passau.server.repository.DiscoveryJobRepository;
import de.uni.passau.server.repository.WorkflowRepository;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduledJobsService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ScheduledJobsService.class);

    @Autowired
    private DiscoveryJobRepository discoveryJobRepository;

    @Autowired
    private DiscoveryJobService discoveryJobService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private WorkflowRepository workflowRepository;

    public void executeDiscoveryJobs() {
        LOGGER.info("EXECUTING (RE)DISCOVERY JOB");
        final var groups = discoveryJobRepository.findAllGroupsByState(DiscoveryJobState.WAITING);

        for (final var group : groups) {
            final var job = discoveryJobRepository.setState(group.job().getId(), DiscoveryJobState.RUNNING);

            LOGGER.info("Functional dependency discovery job {} has started.", job.getId());
            final var dataset = datasetService.getLoadedDataset(group.dataset());

            // TODO Do this async!

            // execute functional dependency discovery
            final List<FDInit> result = executeDiscoveryByApproach(dataset, group.job().approach);
            final WeightedGraph graph = new FDGraphBuilder().buildGraph(result);

            final var jobResult = discoveryJobService.saveResult(group.job().getId(), graph);

            LOGGER.info("Job result: {}", jobResult);

            List<Vertex> dependencyClasses = graph.__getRankedVertices();
            LOGGER.info("RANKED VERTICES: " + graph.__getRankedVertices());

            // TODO create examples


            DiscoveryJobNode updatedJob = discoveryJobRepository.setState(group.job().getId(), DiscoveryJobState.FINISHED);
            LOGGER.info("Job {} has finished.", updatedJob.getId());

            WorkflowNode updatedWorkflow = workflowRepository.setState(group.workflow().getId(), WorkflowState.NEGATIVE_EXAMPLES);
            LOGGER.info("Workflow uuid={} state was updated to {}", updatedWorkflow.getId(), updatedWorkflow.state);

            WorkflowNode updatedWorkflow2 = workflowRepository.setIteration(group.workflow().getId(), group.job().iteration);
            LOGGER.info("Workflow uuid={} iteration was updated to {}", updatedWorkflow2.getId(), updatedWorkflow2.iteration);
        }

        LOGGER.info("(RE)DISCOVERY JOB FINISHED");
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
