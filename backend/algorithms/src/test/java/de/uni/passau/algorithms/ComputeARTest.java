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
import de.uni.passau.core.example.ExampleDecision;
import de.uni.passau.core.example.ExampleRow;

class ComputeARTest {

    /** The initial relation. */
    private Dataset dataset;

    /** Represents the actual functional dependencies. */
    private MaxSets maxSets;

    @BeforeEach
    public void setUp() throws IOException {
        // Setup code for the test, if needed

        // Load the dataset with the CSV file imdb_testadta_paper.csv from resources
        String csvFilePath = "src/test/resources/imdb-title-sample.csv";
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


    void printMaxSets(MaxSets maxSets) {
        maxSets.sets().forEach(maxSet -> {
            System.out.println(maxSet.toString());
        });
    }

    void printArmstrongRelation(ArmstrongRelation ar) {
        System.out.println(String.join(", ", ar.referenceRow));
        for (int i = 0; i < ar.exampleRows.size(); i++) {
            System.out.println(i + ": " + ar.exampleRows.get(i).toString());
        }
    }

    List<ExampleRow> getExampleRowsWithDecision(ArmstrongRelation ar) {
        return ar.exampleRows.stream()
            .filter(exampleRow -> exampleRow.decision != null)
            .toList();
    }

    void printExampleRowsWithDecision(List<ExampleRow> exampleRows) {
        exampleRows.forEach(exampleRow -> {
            System.out.println(exampleRow.toString());
        });
    }

    // Function to set the decision of an ExampleRow. Given is the ArmstrongRelation, a List of ColumnSets to set to Valid and a List of ColumnSets to set to Invalid.
    void setDecision(ArmstrongRelation ar, List<ColumnSet> validSets, List<ColumnSet> invalidSets) {
        ar.exampleRows.forEach(exampleRow -> {
            // Initialize an array for the decision columns with the size of the reference row
            ExampleDecision.DecisionColumn[] decisionColumns = new ExampleDecision.DecisionColumn[ar.referenceRow.length];
            // For each column in the reference row, set the decision column
            for (int i = 0; i < ar.referenceRow.length; i++) {
                decisionColumns[i] = new ExampleDecision.DecisionColumn(
                    ExampleDecision.DecisionColumnStatus.UNDECIDED,
                    // Empty list of strings for reasons
                    List.of()
                );
            }
            // If the lhsSet is in the validSets, then set the decision to ACCEPTED
            if (validSets.contains(exampleRow.lhsSet)) {
                // For all indexe in rhsSet, set the decision column to VALID
                for (int i : exampleRow.rhsSet.toIndexes()) {
                    decisionColumns[i] = new ExampleDecision.DecisionColumn(
                        ExampleDecision.DecisionColumnStatus.VALID,
                        // Empty list of strings for reasons
                        List.of()
                    );
                }
                exampleRow.decision = new ExampleDecision(  
                    ExampleDecision.DecisionStatus.ACCEPTED,
                    decisionColumns
                );
            } 
            // If the lhsSet is in the invalidSets, then set the decision to REJECTED
            else if (invalidSets.contains(exampleRow.lhsSet)) {
                // For all indexe in rhsSet, set the decision column to INVALID
                for (int i : exampleRow.rhsSet.toIndexes()) {
                    decisionColumns[i] = new ExampleDecision.DecisionColumn(
                        ExampleDecision.DecisionColumnStatus.INVALID,
                        // Empty list of strings for reasons
                        List.of()
                    );
                }
                exampleRow.decision = new ExampleDecision(
                    ExampleDecision.DecisionStatus.REJECTED,
                    decisionColumns
                );
            }
        });
    }




    @Test
    void testComputeAR() {
        System.out.println("Computing AR...");

        //MaxSets previousMaxSets = maxSets;
        MaxSets adjustedMaxSets = null;
        MaxSets extendedMaxSets = maxSets;
        //ArmstrongRelation previousAR = null;
        final ArmstrongRelation arInitial = ComputeAR.run(extendedMaxSets, dataset, null, false);
        List<ExampleRow> exampleRowsWithDecision = null;


        System.out.println("\n-- INITIAL STATE --");

        // -- INITIAL STATE --
        // Run the ComputeAR algorithm
        // ar = ComputeAR.run(extendedMaxSets, dataset, null, false);

        // DEBUG: Print the MaxSets (confirmed and candidates)
        System.out.println("MaxSets:");
        printMaxSets(extendedMaxSets);

        // DEBUG: Print the Armstrong Relation
        System.out.println("Armstrong Relation:");
        printArmstrongRelation(arInitial);




        // -- 1. ITERATION --
        System.out.println("\n-- ITERATION 1 --");

        // Get ExampleRows with decision
        exampleRowsWithDecision = getExampleRowsWithDecision(arInitial);
        // DEBUG: Print the ExampleRows with decision
        System.out.println("ExampleRows with decision:");
        printExampleRowsWithDecision(exampleRowsWithDecision);

        // Adjust the MaxSets based on the ExampleRows with decision
        adjustedMaxSets = AdjustMaxSets.run(extendedMaxSets, exampleRowsWithDecision);

        // DEBUG: Print the adjusted MaxSets (confirmed and candidates)
        System.out.println("Adjusted MaxSets:");
        printMaxSets(adjustedMaxSets);

        // Extend the adjusted MaxSets
        extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, 1);

        // DEBUG: Print the extended MaxSets (confirmed and candidates)
        System.out.println("Extended MaxSets:");
        printMaxSets(extendedMaxSets);

        // Create a new ArmstrongRelation with the extended MaxSets
        //previousAR = arInitial;
        ArmstrongRelation ar1 = ComputeAR.run(extendedMaxSets, dataset, arInitial, false);

        // DEBUG: Print the extended Armstrong Relation
        System.out.println("Extended Armstrong Relation:");
        printArmstrongRelation(ar1);

        System.out.println("Set decisions...");
        setDecision(
            ar1,
            List.of(
                ColumnSet.fromIndexes(2), // C
                ColumnSet.fromIndexes(4)  // E
            ),
            List.of(
                ColumnSet.fromIndexes(0)  // A
            )
        );

        // DEBUG: Print the extended Armstrong Relation
        System.out.println("Extended Armstrong Relation with decisions:");
        printArmstrongRelation(ar1);




        
        // -- 2. ITERATION --
        System.out.println("\n-- ITERATION 2 --");

        // Get ExampleRows with decision
        exampleRowsWithDecision = getExampleRowsWithDecision(ar1);

        // DEBUG: Print the ExampleRows with decision
        System.out.println("ExampleRows with decision:");      
        printExampleRowsWithDecision(exampleRowsWithDecision);

        // Adjust the MaxSets based on the ExampleRows with decision
        //previousMaxSets = extendedMaxSets;
        adjustedMaxSets = AdjustMaxSets.run(extendedMaxSets, exampleRowsWithDecision);

        // DEBUG: Print the adjusted MaxSets (confirmed and candidates)
        System.out.println("Adjusted MaxSets:");
        printMaxSets(adjustedMaxSets);

        // Extend the MaxSets again
        extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, 2);

        // DEBUG: Print the extended MaxSets (confirmed and candidates)
        System.out.println("Extended MaxSets:");
        printMaxSets(extendedMaxSets);

        // Create a new ArmstrongRelation with the extended MaxSets
        //previousAR = ar1;
        ArmstrongRelation ar2 = ComputeAR.run(extendedMaxSets, dataset, ar1, false);

        // DEBUG: Print the extended Armstrong Relation
        System.out.println("Extended Armstrong Relation:");
        printArmstrongRelation(ar2);

        System.out.println("Set decisions...");
        setDecision(
            ar2,
            // Accepted sets
            List.of(
                ColumnSet.fromIndexes(2, 3), // CD
                ColumnSet.fromIndexes(2, 4), // CE
                ColumnSet.fromIndexes(3, 4)  // DE
            ),
            List.of()
        );
        
        System.out.println("Extended Armstrong Relation with decisions:");
        printArmstrongRelation(ar2);




        // -- 3. ITERATION --
        System.out.println("\n-- ITERATION 3 --");

        // Get ExampleRows with decision
        exampleRowsWithDecision = getExampleRowsWithDecision(ar2);

        // DEBUG: Print the ExampleRows with decision
        System.out.println("ExampleRows with decision:");
        printExampleRowsWithDecision(exampleRowsWithDecision);

        // Adjust the MaxSets based on the ExampleRows with decision
        //previousMaxSets = extendedMaxSets;
        adjustedMaxSets = AdjustMaxSets.run(extendedMaxSets, exampleRowsWithDecision);
        // DEBUG: Print the adjusted MaxSets (confirmed and candidates)
        System.out.println("Adjusted MaxSets:");
        printMaxSets(adjustedMaxSets);
        // Extend the MaxSets again
        extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, 3);
        // DEBUG: Print the extended MaxSets (confirmed and candidates)
        System.out.println("Extended MaxSets:");
        printMaxSets(extendedMaxSets);
        // Create a new ArmstrongRelation with the extended MaxSets
        //previousAR = ar2;
        ArmstrongRelation ar3 = ComputeAR.run(extendedMaxSets, dataset, ar2, false);
        // DEBUG: Print the extended Armstrong Relation
        System.out.println("Extended Armstrong Relation:");
        printArmstrongRelation(ar3);

        System.out.println("Set decisions...");
        setDecision(
            ar3,
            // Accepted sets
            List.of(
                ColumnSet.fromIndexes(1, 2, 4), // BCE
                ColumnSet.fromIndexes(1, 3, 4), // BDE   
                ColumnSet.fromIndexes(2, 3, 4)  // CDE
            ),
            // Invalid sets
            List.of(
                ColumnSet.fromIndexes(1, 2, 3) // BCD
            )
        );

        System.out.println("Extended Armstrong Relation with decisions:");
        printArmstrongRelation(ar3);    





        // -- 4. ITERATION --
        System.out.println("\n-- ITERATION 4 --");

        // Get ExampleRows with decision
        exampleRowsWithDecision = getExampleRowsWithDecision(ar3);
        // DEBUG: Print the ExampleRows with decision
        System.out.println("ExampleRows with decision:");
        printExampleRowsWithDecision(exampleRowsWithDecision);  
        // Adjust the MaxSets based on the ExampleRows with decision
        //previousMaxSets = extendedMaxSets;
        adjustedMaxSets = AdjustMaxSets.run(extendedMaxSets, exampleRowsWithDecision);
        // DEBUG: Print the adjusted MaxSets (confirmed and candidates)
        System.out.println("Adjusted MaxSets:");
        printMaxSets(adjustedMaxSets);  
        // Extend the MaxSets again
        extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, 4);
        // DEBUG: Print the extended MaxSets (confirmed and candidates)
        System.out.println("Extended MaxSets:");
        printMaxSets(extendedMaxSets);
        // Create a new ArmstrongRelation with the extended MaxSets
        //previousAR = ar3;
        ArmstrongRelation ar4 = ComputeAR.run(extendedMaxSets, dataset, ar3, false);
        // DEBUG: Print the extended Armstrong Relation
        System.out.println("Extended Armstrong Relation:");
        printArmstrongRelation(ar4);
    }

}
