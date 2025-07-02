package de.uni.passau.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.uni.passau.server.model.ExampleRowEntity;
import de.uni.passau.server.repository.ExampleRowRepository;

@RestController
public class ExampleRowController {

    @Autowired
    private ExampleRowRepository exampleRowRepository;

    @GetMapping("/example-rows")
    public List<ExampleRowEntity> getExampleRows() {
        return exampleRowRepository.findAll();
    }

    @GetMapping("/example-row")
    public ExampleRowEntity testExampleRow() {
        final var row = ExampleRowEntity.create("value");

        exampleRowRepository.save(row);

        final var foundRow = exampleRowRepository.findById(row.getId());
        if (!foundRow.isPresent())
            return null;

        return foundRow.get();
    }

}
