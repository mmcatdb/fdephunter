package de.uni.passau.server.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni.passau.server.model.DocumentEntity;
import de.uni.passau.server.repository.DocumentRepository;

/** Just a convenience class so that we don't have to import both repository and mapper. */
@Service
public class StorageService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T get(String id, Class<T> type) {
        final var document = documentRepository.findById(id).get();
        return document.getParsedData(type, objectMapper);
    }

    public Document getAsResponse(String id) {
        final var document = documentRepository.findById(id).get();
        return document.toResponse();
    }

    /**
     * Important note: the data object has to be like a normal object. It can't be list, array, or generally anything that would be serialized as JSON array.
     */
    public void set(String id, Object data) {
        final var document = DocumentEntity.create(id, data, objectMapper);
        documentRepository.save(document);
    }

}
