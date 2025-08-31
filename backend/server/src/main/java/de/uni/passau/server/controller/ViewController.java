package de.uni.passau.server.controller;

import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.service.StorageService;

@RestController
public class ViewController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/workflows/{workflowId}/fds")
    public Document getFds(@PathVariable UUID workflowId) {
        return storageService.getAsResponse(WorkflowEntity.fdsId(workflowId));
    }

    @GetMapping("/workflows/{workflowId}/lattices")
    public Document getLattices(@PathVariable UUID workflowId) {
        return storageService.getAsResponse(WorkflowEntity.latticesId(workflowId));
    }

}
