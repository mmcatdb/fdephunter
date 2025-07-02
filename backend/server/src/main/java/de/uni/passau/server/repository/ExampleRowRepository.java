package de.uni.passau.server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.uni.passau.server.model.ExampleRowEntity;

public interface ExampleRowRepository extends MongoRepository<ExampleRowEntity, UUID> {

    public Optional<ExampleRowEntity> findById(UUID id);

}
