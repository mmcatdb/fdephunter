package de.uni.passau.server.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.model.ApproachNode;
import de.uni.passau.server.repository.ApproachRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ApproachService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApproachService.class);

    @Autowired
    private ApproachRepository approachRepository;

    public Flux<ApproachNode> getAllApproaches() {
        return approachRepository.findAll();
    }

    public Mono<Void> removeWorkflow(ApproachName id) {
        return approachRepository.deleteById(id);
    }

    public Mono<ApproachNode> save(ApproachNode dataset) {
        return approachRepository.save(dataset);
    }

    public Mono<ApproachNode> getApproachByName(ApproachName name) {
        return approachRepository.findById(name);
    }

    public Flux<ApproachNode> initialize() {
        LOGGER.warn("TODO -- INITIALIZE/UPDATE LIST OF ALGORITHMS IN DATABASE BASED ON IMPLEMENTATION IN SOURCE CODE");
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
