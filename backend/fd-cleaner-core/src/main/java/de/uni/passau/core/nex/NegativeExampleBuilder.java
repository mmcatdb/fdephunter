package de.uni.passau.core.nex;

import de.uni.passau.core.approach.FDInit;
import de.uni.passau.core.dataset.DatasetData;
import de.uni.passau.core.graph.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.lang.Nullable;

public class NegativeExampleBuilder {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final Random random = new Random();
    private final int originalRowCount;
    private final String[] header;
    private final List<String[]> relation;

    /**
     * Constructs a new NegativeExampleBuilder for the given relation.
     * @param filename the filename of the CSV file to build negative examples from
     */
    public NegativeExampleBuilder(DatasetData data) {
        this.header = data.getHeader();
        this.relation = data.getRows();
        this.originalRowCount = relation.size();
    }

    /**
     * Builds a negative example from a row in the relation and a column.
     * The value in the given column is replaced with a random, unique value.
     * The decision is set to AcceptedStatus.UNANSWERED and PredefinedReason.NONE.
     * @param row the row in the relation
     * @param rhs the column in the relation to replace
     * @return the negative example
     */
    public NegativeExample createNew(List<FDInit> fds, int row) throws IllegalArgumentException {
        if (fds.isEmpty())
            throw new IllegalArgumentException("No FDs provided.");

        String rhs = fds.get(0).rhs();
        final boolean isMultipleRHS = fds.stream().anyMatch(fd -> !rhs.equals(fd.rhs()));
        if (isMultipleRHS)
            throw new IllegalArgumentException("Provided FDs have multiple different right sides.");
        
        final var values = computeExampleValues(fds, row);

        validateExample(fds, values.innerValues);

        final var view = values.innerValues.keySet().stream().toList();

        return new NegativeExample(generateExampleId(), null, values.innerValues, values.originalValues, view, fds);
    }

    public NegativeExample createNew(Vertex vertex, int row) {
        final String rhs = vertex.__getLabel();
        final List<FDInit> fds = vertex.__getIncomingEdges().stream()
            .map(edge -> Vertex.identifierToLabel(edge.getSource()))
            .map(lhs -> new FDInit(lhs, rhs))
            .toList();

        return createNew(fds, row);
    }

    private void validateExample(List<FDInit> fds, Map<String, String> innerValues) {
        for (FDInit fd : fds) {
            for (String column : fd.lhs())
                if (!innerValues.containsKey(column))
                    throw new IllegalArgumentException("Column " + column + " is not in the negative example.");

            if (!innerValues.containsKey(fd.rhs()))
                throw new IllegalArgumentException("Column " + fd.rhs() + " is not in the negative example.");
        }
    }

    private static String generateExampleId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Primitive method for creating a random, unique value for a cell in a column.
     * The value is created by generating a random string and appending it to itself
     * until it is unique.
     * This method is not suitable for large datasets.
     * TODO: Implement a better method for generating unique values.
     * 
     * @param columnIndex the index of the column
     * 
     * @return a random, unique value for a cell in the column
     */
    private String getUniqueCellValue(int columnIndex) {
        StringBuilder sb = new StringBuilder(7);
        for (int i = 0; i < 7; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(randomIndex);
            sb.append(randomChar);
        }
        String randomString = sb.toString();
        String uniqueValue = randomString;
        while (isValueInColumn(uniqueValue, columnIndex))
            uniqueValue += randomString;
            
        return uniqueValue;
    }

    /**
     * Checks if a value is in a column.
     * @param uniqueValue the value to check
     * @param columnIndex the index of the column
     * @return true if the value is in the column, false otherwise
     */
    private boolean isValueInColumn(String uniqueValue, int columnIndex) {
        return relation.stream().anyMatch(row -> row[columnIndex].equals(uniqueValue));
    }

    /**
     * Returns the next negative example for the given decision.
     * Only to be called when we want to continue with the current negative example
     * (i.e. the same set of FDs).
     */
    public List<NegativeExample> getNextNegativeExamples(NegativeExample negEx, Decision decision, @Nullable List<Decision> decisionHistory) throws IllegalStateException {
        if (decision.getStatus() != Decision.Status.REJECTED)
            throw new IllegalStateException("Decision must be REJECTED.");

        final List<String> view = negEx.view;
        final List<FDInit> fds = negEx.fds;
        final List<String> columnsToIgnore = decision.getProblematicColumns();
        if (columnsToIgnore.isEmpty())
            throw new IllegalStateException("Reject decision must have at least one problematic column.");

        final List<List<String>> newViews = new ArrayList<>();
        for (String column : columnsToIgnore) {
            final List<String> newView = new ArrayList<>(view);
            if (!newView.contains(column))
                throw new IllegalStateException("Column " + column + " not in view.");

            newView.remove(column);
            newViews.add(newView);
        }

        if (!continueWithSameFDs(view, fds))
            throw new IllegalStateException("Cannot continue with the example as no FDs can be violated by the view.");

        final int row = random.nextInt(originalRowCount - 1); // TODO: select same row as before? When should we chose a different row?

        final List<NegativeExample> newNegExs = new ArrayList<>();
        for (final List<String> newView : newViews) {
            final var values = computeExampleValues(fds, row);
            newNegExs.add(new NegativeExample(generateExampleId(), negEx.id, values.innerValues, values.originalValues, newView, fds));
        }
        
        return newNegExs;
    }

    // TODO what about the function above?
    public static NegativeExample createUpdated(NegativeExample example, List<String> view) {
        return new NegativeExample(generateExampleId(), example.id, example.innerValues, example.originalValues, view, example.fds);
    }

    private boolean continueWithSameFDs(List<String> view, List<FDInit> fds) {
        // if we can still violate any of the FDs with the view, we continue with the same FDs
        // if we cannot violate any of the FDs with the view, we choose a new set of FDs
        return fds.stream().anyMatch(fd -> fd.lhs().stream().allMatch(view::contains));
    }

    private static record ExampleValues(Map<String, String> innerValues, Map<String, String> originalValues) {}

    private ExampleValues computeExampleValues(List<FDInit> fds, int row) {
        final Map<String, String> innerValues = new HashMap<>();
        final Map<String, String> originalValues = new HashMap<>();
        final String rhs = fds.get(0).rhs();
        final int rhsIndex = Arrays.asList(header).indexOf(rhs);

        // iterate over columns of relation
        for (int i = 0; i < relation.get(0).length; i++) {
            originalValues.put(header[i], relation.get(row)[i]);
            innerValues.put(header[i],
                i == rhsIndex
                    ? getUniqueCellValue(i)
                    : relation.get(row)[i]
            );
        }

        // final var view = innerValues.keySet().stream().toList();


        return new ExampleValues(innerValues, originalValues);
    }

}
