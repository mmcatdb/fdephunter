package de.uni.passau.server.service;

import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.nex.NegativeExample;
import de.uni.passau.server.model.ClassNode;
import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.model.NegativeExampleNode.NegativeExampleState;
import de.uni.passau.server.model.NegativeExampleNode.Payload;
import de.uni.passau.server.repository.ClassRepository;
import de.uni.passau.server.repository.DiscoveryResultRepository;
import de.uni.passau.server.repository.NegativeExampleRepository;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NegativeExampleService {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NegativeExampleService.class);

    @Autowired
    private NegativeExampleRepository negativeExampleRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private DiscoveryResultRepository discoveryResultRepository;

    @Autowired
    private ObjectMapper objectMapperJSON;

    public Flux<NegativeExample> fetchUnassignedExamples(String workflowId) {
        return negativeExampleRepository.findUnassignedExamplesForWorkflow(workflowId).map(group -> nodeToObject(group.example(), group.lastExampleId()));
    }

    public Mono<ClassNode> createClass(String resultId, Vertex dependencyClass) {
        final var node = ClassNode.createNew(dependencyClass.__getLabel(), dependencyClass.__getWeight());
        return classRepository.save(node).flatMap(classX ->
            discoveryResultRepository.saveHasClass(resultId, classX.getId())
                .then(Mono.just(classX))
        );
    }

    public Mono<NegativeExampleNode> createExample(String classId, NegativeExample negativeExample) {
        final String payload = objectToPayload(negativeExample);
        final var node = NegativeExampleNode.createNew(negativeExample.id, payload);

        return negativeExampleRepository.save(node).flatMap(example ->
            classRepository.saveHasNegativeExample(classId, example.getId())
                .then(
                    negativeExample.previousIterationId == null
                        ? Mono.just(example)
                        : negativeExampleRepository.saveHasPrevious(negativeExample.previousIterationId, example.getId())
                )
        );
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

    private NegativeExample nodeToObject(NegativeExampleNode node, @Nullable String lastExampleId) {
        try {
            final var payload = objectMapperJSON.readValue(node.getPayload(), NegativeExampleNode.Payload.class);
            return new NegativeExample(node.getId(), lastExampleId, payload.innerValues(), payload.originalValues(), payload.view(), payload.fds());
        }
        catch (JsonProcessingException exception) {
            LOGGER.error("Can't deserialize example node: " + node.getId() + "\nwith payload: " + node.getPayload(), exception);
            throw new RuntimeException(exception);
        }
    }

    public Flux<NegativeExample> getAllUnresolvedExamples() {
        return negativeExampleRepository.findAllUnresolved().map(node -> nodeToObject(node, null));
    }

    public Mono<NegativeExampleNode> changeExampleState(String exampleId, NegativeExampleState negativeExampleState) {
        return negativeExampleRepository.saveState(exampleId, negativeExampleState);
    }

}
