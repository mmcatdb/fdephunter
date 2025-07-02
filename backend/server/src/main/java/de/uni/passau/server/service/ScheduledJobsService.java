package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.approach.FDGraphBuilder;
import de.uni.passau.core.approach.FDInit;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.server.approach.HyFDAlgorithm;
import de.uni.passau.server.approach.OurApproachAlgorithm;
import de.uni.passau.server.model.DiscoveryJobNode;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.DiscoveryJobNode.DiscoveryJobState;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
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

            // TODO create examples


            DiscoveryJobNode updatedJob = discoveryJobRepository.setState(group.job().getId(), DiscoveryJobState.FINISHED);
            LOGGER.info("Job {} has finished.", updatedJob.getId());

            WorkflowEntity workflow = group.workflow();

            workflow.state = WorkflowState.NEGATIVE_EXAMPLES;
            workflow.iteration = group.job().iteration;

            workflow = workflowRepository.save(workflow);
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
