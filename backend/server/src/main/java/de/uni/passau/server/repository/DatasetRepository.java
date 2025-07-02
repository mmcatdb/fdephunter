package de.uni.passau.server.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.uni.passau.server.model.DatasetEntity;

public interface DatasetRepository extends MongoRepository<DatasetEntity, UUID> {

}
