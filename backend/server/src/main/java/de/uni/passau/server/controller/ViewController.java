package de.uni.passau.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.Lattice;

@RestController
public class ViewController {

    @GetMapping("/workflows/{workflowId}/fds")
    public FdSet getFds(@PathVariable UUID workflowId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @GetMapping("/workflows/{workflowId}/lattices")
    public List<Lattice> getLattices(@PathVariable UUID workflowId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
