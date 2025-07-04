package de.uni.passau.server.controller;

import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.uni.passau.core.model.Lattice;
import de.uni.passau.core.model.Lattice.CellType;
import de.uni.passau.server.model.WorkflowEntity;
import de.uni.passau.server.repository.WorkflowRepository;
import de.uni.passau.server.service.StorageService;

@RestController
public class ViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    StorageService storageService;

    @Autowired
    WorkflowRepository workflowRepository;

    @GetMapping("/workflows/{workflowId}/fds")
    public Document getFds(@PathVariable UUID workflowId) {
        return storageService.getAsResponse(WorkflowEntity.fdsId(workflowId));
    }

    @GetMapping("/workflows/{workflowId}/lattices")
    // public Document getLattices(@PathVariable UUID workflowId) {
    public List<Lattice> getLattices(@PathVariable UUID workflowId) {
        // return storageService.getAsResponse(WorkflowEntity.latticesId(workflowId));

        LOGGER.warn("Lattices are not implemented. Using mock data instead.");
        return createMockLattices(workflowId);
    }

    private List<Lattice> createMockLattices(UUID workflowId) {
        final var workflow = workflowRepository.findById(workflowId).get();
        return workflow.iteration == 0
            ? createInitialMockLattices()
            : createFinalMockLattices();
    }

    private List<Lattice> createInitialMockLattices() {
        return List.of(
            new Lattice(
                "tconst",
                new String[] { "primaryTitle", "startYear", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.SUBSET, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.INITIAL, CellType.INITIAL, CellType.INITIAL, CellType.CANDIDATE, CellType.CANDIDATE, CellType.CANDIDATE),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "primaryTitle",
                new String[] { "tconst", "startYear", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.CANDIDATE, CellType.CANDIDATE, CellType.INITIAL, CellType.CANDIDATE),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "startYear",
                new String[] { "tconst", "primaryTitle", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.CANDIDATE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.INITIAL, CellType.INITIAL, CellType.CANDIDATE),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "runtimeMinutes",
                new String[] { "tconst", "primaryTitle", "startYear", "genres" },
                List.of(
                    List.of(CellType.CANDIDATE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.INITIAL, CellType.INITIAL, CellType.CANDIDATE),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "genres",
                new String[] { "tconst", "primaryTitle", "startYear", "runtimeMinutes" },
                List.of(
                    List.of(CellType.CANDIDATE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.INITIAL, CellType.INITIAL, CellType.CANDIDATE),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.DERIVED),
                    List.of(CellType.DERIVED)
                )
            )
        );
    }

    private List<Lattice> createFinalMockLattices() {
        return List.of(
            new Lattice(
                "tconst",
                new String[] { "primaryTitle", "startYear", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.SUBSET, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.SUBSET, CellType.SUBSET, CellType.SUBSET, CellType.COINCIDENTAL, CellType.COINCIDENTAL, CellType.COINCIDENTAL),
                    List.of(CellType.COINCIDENTAL, CellType.COINCIDENTAL, CellType.COINCIDENTAL, CellType.COINCIDENTAL),
                    List.of(CellType.ELIMINATED)
                )
            ),
            new Lattice(
                "primaryTitle",
                new String[] { "tconst", "startYear", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.GENUINE, CellType.COINCIDENTAL, CellType.SUBSET, CellType.COINCIDENTAL),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.COINCIDENTAL, CellType.COINCIDENTAL, CellType.COINCIDENTAL),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.ELIMINATED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "startYear",
                new String[] { "tconst", "primaryTitle", "runtimeMinutes", "genres" },
                List.of(
                    List.of(CellType.GENUINE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.SUBSET, CellType.SUBSET, CellType.COINCIDENTAL),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.ELIMINATED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "runtimeMinutes",
                new String[] { "tconst", "primaryTitle", "startYear", "genres" },
                List.of(
                    List.of(CellType.GENUINE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.SUBSET, CellType.SUBSET, CellType.COINCIDENTAL),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.ELIMINATED),
                    List.of(CellType.DERIVED)
                )
            ),
            new Lattice(
                "genres",
                new String[] { "tconst", "primaryTitle", "startYear", "runtimeMinutes" },
                List.of(
                    List.of(CellType.GENUINE, CellType.SUBSET, CellType.SUBSET, CellType.SUBSET),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.FINAL, CellType.FINAL, CellType.ELIMINATED),
                    List.of(CellType.DERIVED, CellType.DERIVED, CellType.DERIVED, CellType.GENUINE),
                    List.of(CellType.DERIVED)
                )
            )
        );
    }

}
