/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.service;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.workflow.model.ApproachNode;
import de.uni.passau.server.workflow.repository.ApproachRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
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
