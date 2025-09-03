package de.uni.passau.server.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.uni.passau.algorithms.ComputeAR;
import de.uni.passau.algorithms.ComputeMaxSets;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.server.model.AssignmentEntity;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.DiscoveryJobPayload;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.model.WorkflowEntity.WorkflowState;
import de.uni.passau.server.repository.AssignmentRepository;
import de.uni.passau.server.repository.WorkflowRepository;

@Component
@Scope("prototype")
public class DiscoveryJobAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryJobAlgorithm.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StorageService storageService;

    private WorkflowEntity workflow;
    private Dataset dataset;
    private List<AssignmentEntity> assignments;

    public void execute(JobEntity job) {
        LOGGER.debug("Discovery job {} for workflow {}", job.id(), job.workflowId);

        final var payload = (DiscoveryJobPayload) job.payload;
        workflow = workflowRepository.findById(job.workflowId).get();
        dataset = datasetService.getLoadedDatasetById(payload.datasetId());

        final var maxSets = ComputeMaxSets.run(dataset);

        workflow.state = WorkflowState.NEGATIVE_EXAMPLES;

        final var initialAR = ComputeAR.run(maxSets, dataset, null, false);
        // All example rows are brand new, so we can create assignments for all of them.
        assignments = initialAR.exampleRows.stream()
            .map(row -> AssignmentEntity.create(job.workflowId, dataset.getHeader(), initialAR.referenceRow, row))
            .toList();

        EvaluationJobAlgorithm.computeViews(dataset, workflow, maxSets, maxSets, storageService);

        storageService.set(workflow.initialMaxSetsId(), maxSets);
        storageService.set(workflow.maxSetsId(), maxSets);
        workflowRepository.save(workflow);
        assignmentRepository.saveAll(assignments);
    }

}
