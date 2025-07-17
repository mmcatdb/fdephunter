package de.uni.passau.algorithms;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.AfterEach;
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

    private Dataset dataset;
    private MaxSets maxSets;
    private String jsonResultFilePath;
    private Boolean debug = false; // Set to true for debugging output

    @BeforeEach
    public void setUp() throws IOException {
        // Load the dataset
        String csvFilePath = "src/test/resources/imdb-title-sample.csv";
        jsonResultFilePath = "src/test/resources/imdb-title-sample-expected-workflow-ar-results.json";
        Boolean hasHeader = true;
        dataset = new CSVDataset(csvFilePath, hasHeader);
        dataset.load();

        // Initialize the MaxSets
        maxSets = ComputeMaxSets.run(dataset);
        // Generate the MaxSets for the IMDB dataset
        //genImdbMaxSets();
    }


    // @AfterEach
    // public void tearDown() {
    //     // Cleanup code for the test, if needed
    // }


    @Test
    void testWorkflowComputeAR() {
        System.out.println("\n-- INITIAL STATE --");
        // Run the ComputeAR algorithm
        ArmstrongRelation ar = ComputeAR.run(maxSets, dataset, null, false);
        if (debug) {
            // Print the initial Armstrong Relation
            System.out.println("Initial Armstrong Relation:");
            printArmstrongRelation(ar);
        }
        // Compare the exampleRows in ar with exampleRows in document "0" from file imdb-title-sample-expected-workflow-ar-results.json
        assertTrue(compareExampleRowsWithExpected(ar, "0", jsonResultFilePath));

        // -- 1. ITERATION --
        // Continue the workflow for the first iteration
        ar = continueWorkflow(ar, 1);
        // Compare the exampleRows in ar with exampleRows in document "1" from file imdb-title-sample-expected-workflow-ar-results.json
        assertTrue(compareExampleRowsWithExpected(ar, "1", jsonResultFilePath));

        // Set decisions for the current iteration
        setDecision(
            ar,
            // Accepted sets
            List.of(
                ColumnSet.fromIndexes(2), // C
                ColumnSet.fromIndexes(4)  // E
            ),
            // Rejected sets
            List.of(
                ColumnSet.fromIndexes(0)  // A
            )
        );

        // -- 2. ITERATION --
        // Continue the workflow for the next iteration
        ar = continueWorkflow(ar, 2);
        // Compare the exampleRows in ar with exampleRows in document "2" from file imdb-title-sample-expected-workflow-ar-results.json
        assertTrue(compareExampleRowsWithExpected(ar, "2", jsonResultFilePath));

        // Set decisions for the current iteration
        setDecision(
            ar,
            // Accepted sets
            List.of(
                ColumnSet.fromIndexes(2, 3), // CD
                ColumnSet.fromIndexes(2, 4), // CE
                ColumnSet.fromIndexes(3, 4)  // DE
            ),
            // Rejected sets
            List.of()
        );

        // -- 3. ITERATION --
        // Continue the workflow for the next iteration
        ar = continueWorkflow(ar, 3);
        // Compare the exampleRows in ar with exampleRows in document "3" from file imdb-title-sample-expected-workflow-ar-results.json
        assertTrue(compareExampleRowsWithExpected(ar, "3", jsonResultFilePath));

        // Set decisions for the current iteration
        setDecision(
            ar,
            // Accepted sets
            List.of(
                ColumnSet.fromIndexes(1, 2, 4), // BCE
                ColumnSet.fromIndexes(1, 3, 4), // BDE
                ColumnSet.fromIndexes(2, 3, 4)  // CDE
            ),
            // Rejected sets
            List.of(
                ColumnSet.fromIndexes(1, 2, 3) // BCD
            )
        );

        // -- 4. ITERATION --
        // Continue the workflow for the next iteration
        ar = continueWorkflow(ar, 4);
        // Compare the exampleRows in ar with exampleRows in document "4" from file imdb-title-sample-expected-workflow-ar-results.json
        assertTrue(compareExampleRowsWithExpected(ar, "4", jsonResultFilePath));
    }


    /**
     * Generates the MaxSets for the IMDB dataset.
     * This method is used to initialize the MaxSets for the IMDB dataset.
     */
    private void genImdbMaxSets() {
        // Initialize the MaxSets
        maxSets = new MaxSets(new ArrayList<>());

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


    private void printMaxSets(MaxSets maxSets) {
        maxSets.sets().forEach(maxSet -> {
            System.out.println(maxSet.toString());
        });
    }

    private void printArmstrongRelation(ArmstrongRelation ar) {
        System.out.println(String.join(", ", ar.referenceRow));
        for (int i = 0; i < ar.exampleRows.size(); i++) {
            System.out.println(i + ": " + ar.exampleRows.get(i).toString());
        }
    }

    private List<ExampleRow> getExampleRowsWithDecision(ArmstrongRelation ar) {
        return ar.exampleRows.stream()
            .filter(exampleRow -> exampleRow.decision != null)
            .toList();
    }

    private void printExampleRowsWithDecision(List<ExampleRow> exampleRows) {
        exampleRows.forEach(exampleRow -> {
            System.out.println(exampleRow.toString());
        });
    }


    /**
     * Sets the decisions for the ExampleRows in the ArmstrongRelation based on the valid and invalid sets.
     *
     * @param ar The ArmstrongRelation containing the ExampleRows.
     * @param validSets The list of ColumnSets that are considered valid.
     * @param invalidSets The list of ColumnSets that are considered invalid.
     */
    private void setDecision(ArmstrongRelation ar, List<ColumnSet> validSets, List<ColumnSet> invalidSets) {
        //System.out.println("Setting decisions...");
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

    /**
     * Continues the workflow of the ComputeAR algorithm for a given iteration.
     *
     * @param ar The current ArmstrongRelation.
     * @param iteration The current iteration number.
     * @param debug Whether to print debug information.
     * @return The updated ArmstrongRelation after the iteration.
     */
    private ArmstrongRelation continueWorkflow(ArmstrongRelation ar, int iteration) {
        System.out.println("\n-- CONTINUING WORKFLOW ITERATION " + iteration + " --");
        // 1. Get ExampleRows which have a decision
        List<ExampleRow> exampleRowsWithDecision = getExampleRowsWithDecision(ar);
        // 2. Adjust the MaxSets based on the ExampleRows with decision.
        MaxSets adjustedMaxSets = AdjustMaxSets.run(maxSets, exampleRowsWithDecision);
        // 3. Extend the MaxSets (create new candidates)
        MaxSets extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, iteration);
        maxSets = extendedMaxSets; // Update the maxSets for the next iteration
        // 4. Create a new ArmstrongRelation with the extended MaxSets
        ArmstrongRelation newAR = ComputeAR.run(extendedMaxSets, dataset, ar, false);
        // DEBUG: Print the ExampleRows with decision, adjusted MaxSets, extended MaxSets and the new Armstrong Relation
        if (debug) {
            System.out.println("ExampleRows with decision:");
            printExampleRowsWithDecision(exampleRowsWithDecision);
            System.out.println("Adjusted MaxSets:");
            printMaxSets(adjustedMaxSets);
            System.out.println("Extended MaxSets:");
            printMaxSets(extendedMaxSets);
            System.out.println("New Armstrong Relation:");
            printArmstrongRelation(newAR);
        }
        return newAR;
    }

    /**
     * Compares the ExampleRows in the ArmstrongRelation with the expected results from the JSON file.
     *
     * @param ar The ArmstrongRelation containing the ExampleRows to compare.
     * @param documentId The document ID (e.g., "0", "1", "2", etc.) in the JSON file.
     * @param jsonFilePath The path to the JSON file containing expected results.
     * @return true if the comparison matches, false otherwise.
     */
    private boolean compareExampleRowsWithExpected(ArmstrongRelation ar, String documentId, String jsonFilePath) {
        try {
            // Read and parse the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            JsonNode rootNode = objectMapper.readTree(jsonContent);

            // Get the document for the specified ID
            JsonNode documentNode = rootNode.get(documentId);
            if (documentNode == null) {
                System.err.println("Document with ID '" + documentId + "' not found in JSON file.");
                return false;
            }

            // Get the expected example rows
            JsonNode exampleRowsNode = documentNode.get("exampleRows");
            if (exampleRowsNode == null) {
                System.err.println("ExampleRows not found in document '" + documentId + "'.");
                return false;
            }

            // Check if the number of example rows matches
            if (ar.exampleRows.size() != exampleRowsNode.size()) {
                System.err.println("Number of example rows mismatch. Expected: " + exampleRowsNode.size() +
                                 ", Actual: " + ar.exampleRows.size());
                return false;
            }

            // Compare each example row
            for (int i = 0; i < ar.exampleRows.size(); i++) {
                ExampleRow actualRow = ar.exampleRows.get(i);
                JsonNode expectedRowNode = exampleRowsNode.get(i);

                // Compare values
                JsonNode expectedValuesNode = expectedRowNode.get("values");
                String[] expectedValues = new String[expectedValuesNode.size()];
                for (int j = 0; j < expectedValuesNode.size(); j++) {
                    expectedValues[j] = expectedValuesNode.get(j).asText();
                }

                if (!Arrays.equals(actualRow.values, expectedValues)) {
                    System.err.println("Values mismatch at row " + i + ". Expected: " +
                                     Arrays.toString(expectedValues) + ", Actual: " + Arrays.toString(actualRow.values));
                    return false;
                }

                // Compare LHS
                JsonNode expectedLhsNode = expectedRowNode.get("lhs");
                int[] expectedLhs = new int[expectedLhsNode.size()];
                for (int j = 0; j < expectedLhsNode.size(); j++) {
                    expectedLhs[j] = expectedLhsNode.get(j).asInt();
                }

                if (!Arrays.equals(actualRow.lhsSet.toIndexes(), expectedLhs)) {
                    System.err.println("LHS mismatch at row " + i + ". Expected: " +
                                     Arrays.toString(expectedLhs) + ", Actual: " + Arrays.toString(actualRow.lhsSet.toIndexes()));
                    return false;
                }

                // Compare RHS
                JsonNode expectedRhsNode = expectedRowNode.get("rhs");
                int[] expectedRhs = new int[expectedRhsNode.size()];
                for (int j = 0; j < expectedRhsNode.size(); j++) {
                    expectedRhs[j] = expectedRhsNode.get(j).asInt();
                }

                if (!Arrays.equals(actualRow.rhsSet.toIndexes(), expectedRhs)) {
                    System.err.println("RHS mismatch at row " + i + ". Expected: " +
                                     Arrays.toString(expectedRhs) + ", Actual: " + Arrays.toString(actualRow.rhsSet.toIndexes()));
                    return false;
                }
            }

            System.out.println("✓ Comparison with expected result in document '" + documentId + "' passed successfully!");
            return true;

        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error during comparison: " + e.getMessage());
            return false;
        }
    }
}
