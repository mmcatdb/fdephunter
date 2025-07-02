package de.uni.passau.server.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.uni.passau.server.model.ExampleRowEntity;

public interface ExampleRowRepository extends MongoRepository<ExampleRowEntity, String> {

    public Optional<ExampleRowEntity> findById(String id);

}
