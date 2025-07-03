package de.uni.passau.algorithms;


import java.io.IOException;
import java.util.List;

//import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;

import de.uni.passau.core.example.ArmstrongRelation;


class ComputeARTest {

    /* The initial relation. */
     private Dataset dataset;

    /* Represents the actual functional dependencies. */
    private List<MaxSet> maxSets;


    @BeforeEach
    public void setUp() throws IOException {
        // Setup code for the test, if needed

        // Load the dataset with the CSV file imdb_testadta_paper.csv from resources
        String csvFilePath = "src/test/resources/imdb_testdata_paper.csv";
        Boolean hasHeader = true; // Assuming the CSV has a header
        dataset = new CSVDataset(csvFilePath, hasHeader);
        dataset.load();

        maxSets = new java.util.ArrayList<>();

        // Initialize the maxSets 
        // BE -> A
        // BD -> A
        // BC -> A
        maxSets.add( 
            new MaxSet(
                0,
                List.of(
                    ColumnSet.fromIndexes(new int[]{1, 4}), // BE
                    ColumnSet.fromIndexes(new int[]{1, 3}), // BD
                    ColumnSet.fromIndexes(new int[]{1, 2})  // BC
                )
            )
        );

        // D -> B
        maxSets.add( 
            new MaxSet(
                1,
                List.of(
                    ColumnSet.fromIndexes(new int[]{3}) // D
                )
            )
        );

        // BE -> C
        // BD -> C
        maxSets.add( 
            new MaxSet(
                2,
                List.of(
                    ColumnSet.fromIndexes(new int[]{1, 4}), // BE
                    ColumnSet.fromIndexes(new int[]{1, 3})  // BD
                )
            )
        );

        // BE -> D
        // BC -> D
        maxSets.add( 
            new MaxSet(
                3,
                List.of(
                    ColumnSet.fromIndexes(new int[]{1, 4}), // BE
                    ColumnSet.fromIndexes(new int[]{1, 2})  // BD
                )
            )
        );

        // BD -> E
        // BC -> E
        maxSets.add( 
            new MaxSet(
                4,
                List.of(
                    ColumnSet.fromIndexes(new int[]{1, 3}), // BD
                    ColumnSet.fromIndexes(new int[]{1, 2})  // BC
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
        ArmstrongRelation ar = ComputeAR.run(maxSets, dataset);

    }

}
