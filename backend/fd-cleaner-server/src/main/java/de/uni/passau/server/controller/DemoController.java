/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.controller;

import de.uni.passau.server.approach.Approach1Metadata;
import de.uni.passau.server.approach.HyFDMetadata;
import de.uni.passau.server.workflow.model.ApproachNode;
import de.uni.passau.server.workflow.model.DatasetNode;
import de.uni.passau.server.workflow.model.RoleNode;
import de.uni.passau.server.workflow.model.RoleNode.RoleType;
import de.uni.passau.server.workflow.model.UserNode;
import de.uni.passau.server.workflow.model.WorkflowNode;
import de.uni.passau.server.workflow.service.ApproachService;
import de.uni.passau.server.workflow.service.DatasetService;
import de.uni.passau.server.workflow.service.UserService;
import de.uni.passau.server.workflow.service.WorkflowService;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author pavel.koupil
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ApproachService approachService;

    @Autowired
    private UserService userService;

    @PostMapping("/demo/initialize")
    public Mono<String> initialize() {
        return initializeDatabase();
    }

    private Mono<String> initializeDatabase() {

        var expertRole = List.of(RoleNode.createNew(RoleType.EXPERT));
        var ownerRole = List.of(RoleNode.createNew(RoleType.OWNER));

        var owner = UserNode.createNew("James", "Gosling", "james@java.com", ownerRole);
        var workflow = WorkflowNode.createNew();
        workflow.setId("710c85bf-def8-4b38-a92e-fdd9ae372a95");

        // remove all data from database
        workflowService.purge()
            // initialize approaches
            .then(Mono.defer(() -> {
                LOGGER.info("Initializing approaches.");
                Mono<ApproachNode> hyfdNode = approachService.save(ApproachNode.fromMetadata(new HyFDMetadata()));
                Mono<ApproachNode> approach1Node = approachService.save(ApproachNode.fromMetadata(new Approach1Metadata()));
                return Mono.when(hyfdNode, approach1Node);
            }))
            // initialize datasets
            .then(Mono.defer(() -> {
                LOGGER.info("Initializing datasets.");
                List<DatasetNode> datasets = new ArrayList<>();
                datasets.add(new DatasetNode(null, "iris", DatasetNode.DatasetType.CSV, "data/iris.csv", "iris", null, null, null, null));
                datasets.add(new DatasetNode(null, "balance-scale", DatasetNode.DatasetType.CSV, "data/balance-scale.csv", "balance-scale", null, null, null, null));
                datasets.add(new DatasetNode(null, "chess", DatasetNode.DatasetType.CSV, "data/chess.csv", "chess", null, null, null, null));
                datasets.add(new DatasetNode(null, "abalone", DatasetNode.DatasetType.CSV, "data/abalone.csv", "abalone", null, null, null, null));
                datasets.add(new DatasetNode(null, "nursery", DatasetNode.DatasetType.CSV, "data/nursery.csv", "nursery", null, null, null, null));
                datasets.add(new DatasetNode(null, "breast-cancer-wisconsin", DatasetNode.DatasetType.CSV, "data/breast-cancer-wisconsin.csv", "breast-cancer-wisconsin", null, null, null, null));
                datasets.add(new DatasetNode(null, "bridges", DatasetNode.DatasetType.CSV, "data/bridges.csv", "bridges", null, null, null, null));
                datasets.add(new DatasetNode(null, "echocardiogram", DatasetNode.DatasetType.CSV, "data/echocardiogram.csv", "echocardiogram", null, null, null, null));
                datasets.add(new DatasetNode(null, "adult", DatasetNode.DatasetType.CSV, "data/adult.csv", "adult", null, null, null, null));
                datasets.add(new DatasetNode(null, "letter", DatasetNode.DatasetType.CSV, "data/letter.csv", "letter", null, null, null, null));
                datasets.add(new DatasetNode(null, "ncvoter", DatasetNode.DatasetType.CSV, "data/ncvoter.csv", "ncvoter", null, null, null, null));
                datasets.add(new DatasetNode(null, "hepatitis", DatasetNode.DatasetType.CSV, "data/hepatitis.csv", "hepatitis", null, null, null, null));
                datasets.add(new DatasetNode(null, "horse", DatasetNode.DatasetType.CSV, "data/horse.csv", "horse", null, null, null, null));
                datasets.add(new DatasetNode(null, "fd-reduced-30", DatasetNode.DatasetType.CSV, "data/fd-reduced-30.csv", "fd-reduced-30", null, null, null, null));
                datasets.add(new DatasetNode(null, "plista", DatasetNode.DatasetType.CSV, "data/plista.csv", "plista", null, null, null, null));
                datasets.add(new DatasetNode(null, "flight", DatasetNode.DatasetType.CSV, "data/flight.csv", "flight", null, null, null, null));
                datasets.add(new DatasetNode(null, "uniprot", DatasetNode.DatasetType.CSV, "data/uniprot.csv", "uniprot", null, null, null, null));
                datasets.add(new DatasetNode(null, "lineitem", DatasetNode.DatasetType.CSV, "data/lineitem.csv", "lineitem", null, null, null, null));

                Flux<DatasetNode> allDatasetNodes = datasetService.saveAll(datasets);
                return Mono.when(allDatasetNodes);
            }))
            // initialize users roles
            .then(Mono.defer(() -> {
                LOGGER.info("Initializing user roles.");
                Flux<RoleNode> roles = userService.initializeRoles();
                return Mono.when(roles);
            }))
            // initialize users
            .then(Mono.defer(() -> {
                LOGGER.info("Initializing users.");
                Mono<UserNode> user1 = userService.saveUser(UserNode.createNew("Albert", "Einstein", "albert@has.no.email", expertRole));
                Mono<UserNode> user2 = userService.saveUser(UserNode.createNew("Alan", "Turing", "alan@has.no.email", expertRole));
                Mono<UserNode> user3 = userService.saveUser(UserNode.createNew("Ray", "Tomlinson", "ray@has.no.email", expertRole));
                Mono<UserNode> user4 = userService.saveUser(UserNode.createNew("John", "McCarthy", "john@has.no.email", expertRole));
                Mono<UserNode> user5 = userService.saveUser(owner);
                Mono<UserNode> user6 = userService.saveUser(UserNode.createNew("Margaret", "Hamilton", "margaret@has.no.email", expertRole));
                return Mono.when(user1, user2, user3, user4, user5, user6);
            }))
            // initialize workflow
            .then(Mono.defer(() -> {
                LOGGER.info("Initializing workflow.");
                return workflowService.saveWorkflow(workflow);
            }))
            // add owner to workflow
            .then(Mono.defer(() -> {
                LOGGER.info("Adding owner to workflow.");
                return userService.makeOwnerOfWorkflow(owner.getId(), workflow.getId());
            }))
            .subscribe();
        return Mono.just("{\"message\" : \"ok\"}");
    }

}
