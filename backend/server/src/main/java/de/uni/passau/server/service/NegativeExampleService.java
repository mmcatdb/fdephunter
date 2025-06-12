package de.uni.passau.server.service;

import de.uni.passau.core.example.NegativeExample;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.server.model.ClassNode;
import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.Payload;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.JobResultRepository;
import de.uni.passau.server.repository.NegativeExampleRepository;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegativeExampleService {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NegativeExampleService.class);

    @Autowired
    private NegativeExampleRepository negativeExampleRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private JobResultRepository jobResultRepository;

    @Autowired
    private ObjectMapper objectMapperJSON;

    public List<NegativeExample> fetchUnassignedExamples(String workflowId) {
        return negativeExampleRepository.findUnassignedExamplesForWorkflow(workflowId).stream()
            .map(group -> nodeToObject(group.example(), group.lastExampleId()))
            .toList();
    }

    public ClassNode createClass(String resultId, Vertex dependencyClass) {
        var clazz = ClassNode.createNew(dependencyClass.__getLabel(), dependencyClass.__getWeight());

        clazz = classRepository.save(clazz);
        jobResultRepository.saveHasClass(resultId, clazz.getId());

        return clazz;
    }

    public NegativeExampleNode createExample(String classId, NegativeExample example) {
        final String payload = objectToPayload(example);
        var node = NegativeExampleNode.createNew(example.id, payload);

        node = negativeExampleRepository.save(node);
        classRepository.saveHasNegativeExample(classId, node.getId());

        if (example.previousIterationId != null)
            node = negativeExampleRepository.saveHasPrevious(example.previousIterationId, node.getId());

        return node;
    }

    private String objectToPayload(NegativeExample example) {
        final var payload = new Payload(example.innerValues, example.originalValues, example.view, example.fds, example.getValues());
        try {
            return objectMapperJSON.writeValueAsString(payload);
        }
        catch (JsonProcessingException exception) {
            LOGGER.error("Can't serialize example payload: " + example.toString(), exception);
            throw new RuntimeException(exception);
        }
    }

    public List<NegativeExample> getAllUnresolvedExamples() {
        return negativeExampleRepository.findAllUnresolved().stream()
            .map(node -> nodeToObject(node, null))
            .toList();
    }

    private NegativeExample nodeToObject(NegativeExampleNode node, @Nullable String lastExampleId) {
        try {
            final var payload = objectMapperJSON.readValue(node.payload, NegativeExampleNode.Payload.class);
            return new NegativeExample(node.getId(), lastExampleId, payload.innerValues(), payload.originalValues(), payload.view(), payload.fds());
        }
        catch (JsonProcessingException exception) {
            LOGGER.error("Can't deserialize example node: " + node.getId() + "\nwith payload: " + node.payload, exception);
            throw new RuntimeException(exception);
        }
    }

}
