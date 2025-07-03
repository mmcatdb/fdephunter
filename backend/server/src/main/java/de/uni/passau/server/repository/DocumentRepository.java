package de.uni.passau.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.uni.passau.server.model.DocumentEntity;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {

}
