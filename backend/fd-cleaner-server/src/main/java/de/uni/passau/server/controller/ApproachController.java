package de.uni.passau.server.controller;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.model.ApproachNode;
import de.uni.passau.server.service.ApproachService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ApproachController {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ApproachController.class);

    @Autowired
    private ApproachService approachService;

    @GetMapping("/approaches")
    public Flux<ApproachNode> getAllApproaches() {
        return approachService.getAllApproaches();
    }

    @Deprecated
    @GetMapping("/approaches/{name}")
    public Mono<ApproachNode> getApproachByName(@PathVariable ApproachName name) {
        return approachService.getApproachByName(name);
    }

    @Deprecated
    @GetMapping("/approaches/initialize")
    public Flux<ApproachNode> initilizeApproaches() {
        return approachService.initialize();
    }

}
