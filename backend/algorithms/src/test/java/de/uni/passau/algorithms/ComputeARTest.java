package de.uni.passau.algorithms;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private Dataset dataset;
    private MaxSets maxSets;
    private static final String csvFilePath = "src/test/resources/imdb-title-sample.csv";
    private static final String jsonResultFilePath = "src/test/resources/imdb-title-sample-expected-workflow-ar-results.json";
    private static final Boolean debug = true; // Set to true for debugging output

    @BeforeEach
    public void setUp() throws IOException {
        // Load the dataset
        boolean hasHeader = true;
        dataset = new CSVDataset(csvFilePath, hasHeader, ',');
        dataset.load();

        // Initialize the MaxSets
        maxSets = ComputeMaxSets.run(dataset);
        // Generate the MaxSets for the IMDB dataset
        //genImdbMaxSets();
    }

    @Test
    void testWorkflowComputeAR() {
        System.out.println("-- INITIAL STATE --\n");
        // Run the ComputeAR algorithm
        ArmstrongRelation ar = ComputeAR.run(maxSets, dataset, null, false);
        if (debug) {
            // Print the initial Armstrong Relation
            System.out.println("Initial Armstrong relation:");
            System.out.println(ar.exampleFdsToString());
        }
        // Compare the exampleRows in ar with exampleRows in document "0" from file imdb-title-sample-expected-workflow-ar-results.json
        checkExampleRows(ar, "0", jsonResultFilePath);

        // -- 1. ITERATION --
        ar = continueWorkflow(ar, 1);
        checkExampleRows(ar, "1", jsonResultFilePath);

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
        ar = continueWorkflow(ar, 2);
        checkExampleRows(ar, "2", jsonResultFilePath);

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
        ar = continueWorkflow(ar, 3);
        checkExampleRows(ar, "3", jsonResultFilePath);

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
        ar = continueWorkflow(ar, 4);
        checkExampleRows(ar, "4", jsonResultFilePath);
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
     * @return The updated ArmstrongRelation after the iteration.
     */
    private ArmstrongRelation continueWorkflow(ArmstrongRelation ar, int iteration) {
        System.out.println("\n-- CONTINUING WORKFLOW ITERATION " + iteration + " --\n");
        // 1. Get ExampleRows which have a decision
        List<ExampleRow> exampleRowsWithDecision = getExampleRowsWithDecision(ar);
        // 2. Adjust the MaxSets based on the ExampleRows with decision.
        MaxSets adjustedMaxSets = AdjustMaxSets.run(maxSets, exampleRowsWithDecision, false);
        // 3. Extend the MaxSets (create new candidates)
        MaxSets extendedMaxSets = ExtendMaxSets.run(adjustedMaxSets, iteration);
        maxSets = extendedMaxSets; // Update the maxSets for the next iteration
        // 4. Create a new ArmstrongRelation with the extended MaxSets
        ArmstrongRelation newAR = ComputeAR.run(extendedMaxSets, dataset, ar, false);
        // DEBUG: Print the ExampleRows with decision, adjusted MaxSets, extended MaxSets and the new Armstrong Relation
        if (debug) {
            System.out.println("ExampleRows with decision:");
            printExampleRowsWithDecision(exampleRowsWithDecision);
            System.out.println("\nAdjusted MaxSets:");
            printMaxSets(adjustedMaxSets);
            System.out.println("\nExtended MaxSets:");
            printMaxSets(extendedMaxSets);
            System.out.println("\nNew Armstrong Relation:");
            System.out.println(newAR.exampleFdsToString());
            // System.out.println("");
            // System.out.println(ar.tableToString());
        }
        return newAR;
    }

    /**
     * Compares the ExampleRows in the ArmstrongRelation with the expected results from the JSON file.
     *
     * @param ar The ArmstrongRelation containing the ExampleRows to compare.
     * @param documentId The document ID (e.g., "0", "1", "2", etc.) in the JSON file.
     * @param jsonFilePath The path to the JSON file containing expected results.
     */
    private void checkExampleRows(ArmstrongRelation ar, String documentId, String jsonFilePath) {
        // Read and parse the JSON file
        final ObjectMapper objectMapper = new ObjectMapper();

        final JsonNode rootNode = assertDoesNotThrow(() -> {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            return objectMapper.readTree(jsonContent);
        });

        // Get the document for the specified ID
        final JsonNode documentNode = rootNode.get(documentId);
        assertNotNull(documentNode, "Document with ID '" + documentId + "' not found in JSON file.");

        // Get the expected example rows
        final JsonNode exampleRowsNode = documentNode.get("exampleRows");
        assertNotNull(exampleRowsNode, "ExampleRows not found in document '" + documentId + "'.");

        // Check if the number of example rows matches
        assertEquals(ar.exampleRows.size(), exampleRowsNode.size(), "Number of example rows mismatch");

        // Compare each example row
        for (int i = 0; i < ar.exampleRows.size(); i++) {
            final ExampleRow actualRow = ar.exampleRows.get(i);
            final JsonNode expectedRowNode = exampleRowsNode.get(i);

            // Compare LHS
            final JsonNode expectedLhsNode = expectedRowNode.get("lhs");
            final ColumnSet expectedLhs = ColumnSet.fromIndexes();
            for (int j = 0; j < expectedLhsNode.size(); j++)
                expectedLhs.set(expectedLhsNode.get(j).asInt());

            assertEquals(actualRow.lhsSet, expectedLhs, "LHS mismatch at row " + i);

            // Compare RHS
            final JsonNode expectedRhsNode = expectedRowNode.get("rhs");
            final ColumnSet expectedRhs = ColumnSet.fromIndexes();
            for (int j = 0; j < expectedRhsNode.size(); j++)
                expectedRhs.set(expectedRhsNode.get(j).asInt());

            assertEquals(actualRow.rhsSet, expectedRhs, "RHS mismatch at row " + i);

            // Compare values
            final JsonNode expectedValuesNode = expectedRowNode.get("values");
            final String[] expectedValues = new String[expectedValuesNode.size()];
            for (int j = 0; j < expectedValuesNode.size(); j++)
                expectedValues[j] = expectedValuesNode.get(j).asText();

            assertArrayEquals(actualRow.values, expectedValues, "Values mismatch at row " + i);
        }
    }
}
