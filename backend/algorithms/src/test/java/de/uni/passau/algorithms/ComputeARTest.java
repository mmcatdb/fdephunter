package de.uni.passau.algorithms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.example.ArmstrongRelation;

class ComputeARTest {

    /** The initial relation. */
    private Dataset dataset;

    /** Represents the actual functional dependencies. */
    private MaxSets maxSets;

    @BeforeEach
    public void setUp() throws IOException {
        // Setup code for the test, if needed

        // Load the dataset with the CSV file imdb_testadta_paper.csv from resources
        String csvFilePath = "src/test/resources/imdb_testdata_paper.csv";
        Boolean hasHeader = true; // Assuming the CSV has a header
        dataset = new CSVDataset(csvFilePath, hasHeader);
        dataset.load();

        maxSets = new MaxSets(new ArrayList<>());

        // Initialize the maxSets
        // BE -> A
        // BD -> A
        // BC -> A
        maxSets.sets().add(
            new MaxSet(
                0,
                List.of(
                    ColumnSet.fromIndexes(1, 4), // BE
                    ColumnSet.fromIndexes(1, 3), // BD
                    ColumnSet.fromIndexes(1, 2)  // BC
                )
            )
        );

        // D -> B
        maxSets.sets().add(
            new MaxSet(
                1,
                List.of(
                    ColumnSet.fromIndexes(3) // D
                )
            )
        );

        // BE -> C
        // BD -> C
        maxSets.sets().add(
            new MaxSet(
                2,
                List.of(
                    ColumnSet.fromIndexes(1, 4), // BE
                    ColumnSet.fromIndexes(1, 3)  // BD
                )
            )
        );

        // BE -> D
        // BC -> D
        maxSets.sets().add(
            new MaxSet(
                3,
                List.of(
                    ColumnSet.fromIndexes(1, 4), // BE
                    ColumnSet.fromIndexes(1, 2)  // BD
                )
            )
        );

        // BD -> E
        // BC -> E
        maxSets.sets().add(
            new MaxSet(
                4,
                List.of(
                    ColumnSet.fromIndexes(1, 3), // BD
                    ColumnSet.fromIndexes(1, 2)  // BC
                )
            )
        );
    }

    // @AfterEach
    // public void tearDown() {
    //     // Cleanup code for the test, if needed
    // }

    @Test
    void testComputeAR() {
        System.out.println("Computing AR...");

        // Run the ComputeAR algorithm
        Integer iteration = 1;
        ArmstrongRelation ar = ComputeAR.run(maxSets, dataset, null, false);
    }

}
