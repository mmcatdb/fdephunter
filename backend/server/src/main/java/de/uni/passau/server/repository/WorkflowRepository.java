package de.uni.passau.server.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import de.uni.passau.server.model.WorkflowEntity;

public interface WorkflowRepository extends MongoRepository<WorkflowEntity, UUID> {

}
