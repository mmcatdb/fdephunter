package de.uni.passau.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import de.uni.passau.server.model.JobEntity;
import de.uni.passau.server.model.JobEntity.JobState;

public interface JobRepository extends MongoRepository<JobEntity, UUID> {

    @Query(sort = "{ 'index' : -1 }")
    public JobEntity findLastByWorkflowId(UUID workflowId);

    public List<JobEntity> findAllByState(JobState state);

    public int countByWorkflowId(UUID workflowId);

}
