package de.uni.passau.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.uni.passau.server.model.AssignmentEntity;

public interface AssignmentRepository extends MongoRepository<AssignmentEntity, UUID> {

    List<AssignmentEntity> findAllByWorkflowId(UUID workflowId);

    List<AssignmentEntity> findAllByWorkflowIdAndIsActive(UUID workflowId, boolean isActive);

}
