package de.uni.passau.server.service;

import de.uni.passau.core.exception.NamedException;
import de.uni.passau.core.exception.OtherException;
import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.EvaluationJobPayload;
import de.uni.passau.server.model.JobEntity.DiscoveryJobPayload;
import de.uni.passau.server.model.JobEntity.JobState;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.repository.JobRepository;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskExecutor asyncExecutor;

    @Autowired
    private ObjectProvider<DiscoveryJobAlgorithm> discoveryJobAlgorithmProvider;

    @Autowired
    private ObjectProvider<EvaluationJobAlgorithm> evaluationJobAlgorithmProvider;

    public JobEntity createDiscoveryJob(WorkflowEntity workflow, String description) {
        // This is supposed to be the first job in the workflow, so index is 0.
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.id());
        if (prevJobsCount != 0)
            throw new IllegalStateException("Cannot create a discovery job when there are already jobs in the workflow.");

        final var payload = new DiscoveryJobPayload(workflow.datasetId);
        final var job = JobEntity.create(workflow.id(), 0, description, payload);
        return jobRepository.save(job);
    }

    public JobEntity createEvaluationJob(WorkflowEntity workflow, String description) {
        final var prevJobsCount = jobRepository.countByWorkflowId(workflow.id());
        final var payload = new EvaluationJobPayload();
        final var job = JobEntity.create(workflow.id(), prevJobsCount, description, payload);
        return jobRepository.save(job);
    }

    public void executeJobAsync(UUID jobId) {
        final var jobTask = new JobTask(jobId);
        asyncExecutor.execute(jobTask);
    }

    private class JobTask implements Runnable {

        private final UUID jobId;

        public JobTask(UUID jobId) {
            this.jobId = jobId;
        }

        @Override public void run() {
            executeJob(jobId);
        }

    }

    private void executeJob(UUID jobId) {
        var job = jobRepository.findById(jobId).get();
        if (job.state != JobState.WAITING) {
            LOGGER.warn("Job { id: {}, name: '{}' } is not ready.", job.id(), job.description);
            return;
        }

        job.state = JobState.RUNNING;
        job.startedAt = new java.util.Date();
        job = jobRepository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id(), job.description);

        try {
            executeJobPayload(job);
            job.state = JobState.FINISHED;
            job.finishedAt = new java.util.Date();
            job = jobRepository.save(job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id(), job.description);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);

            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id(), job.description), finalException);
            job.state = JobState.FAILED;
            job.finishedAt = new java.util.Date();
            job.error = finalException.toSerializedException();
            job = jobRepository.save(job);
        }
    }

    private void executeJobPayload(JobEntity job) {
        // NICE_TO_HAVE Change this to switch once we are on a better java version.
        if (job.payload instanceof DiscoveryJobPayload)
            discoveryJobAlgorithmProvider.getObject().execute(job);
        else if (job.payload instanceof EvaluationJobPayload)
            evaluationJobAlgorithmProvider.getObject().execute(job);
    }

}
