package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeARException;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComputeAR {

    /**
     * Computes Armstrong relation for functional dependencies defined by a maximal set.
     * An original relation is needed to provide values for the generated relation, but it's not used to determine the functional dependencies.
     *
     * @param maxSets - Represents the actual functional dependencies.
     * @param dataset - The initial relation. It's used only to provide values for the generated relation, not to determine the functional dependencies.
     * @param prev - If null, we start from scratch. Otherwise, we keep the previous values.
     */
    public static ArmstrongRelation run(
        MaxSets maxSets,
        Dataset dataset,
        @Nullable ArmstrongRelation prev,
        boolean isEvaluatingPositives
    ) {
        try {
            final var algorithm = new ComputeAR(maxSets, dataset, prev, isEvaluatingPositives);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeARException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final Dataset dataset;
    private final @Nullable ArmstrongRelation prev;
    private boolean isEvaluatingPositives;

    private final int columnsCount;

    private ComputeAR(MaxSets maxSets, Dataset dataset, @Nullable ArmstrongRelation prev, boolean isEvaluatingPositives) {
        this.maxSets = maxSets;
        this.dataset = dataset;
        this.prev = prev;
        this.isEvaluatingPositives = isEvaluatingPositives;

        this.columnsCount = maxSets.sets().size();
    }

    // ## How this algorithm works
    //
    // For each class X, we take all elements from its max set (let's call such an element LHS).
    // Each LHS then corresponds to a row where each value from the LHS is the same as in the reference row and each other value is completely unique.
    //
    // ## Why is this algorithm legit
    //
    // ### We don't break any FD that holds.
    //
    // Let's say that M_X (max set for class X) contains an element LHS. By creating the row with LHS, we break all dependencies LHS => *.
    // For contradiction, let's say that LHS => Y holds for some class Y != X.
    // Therefore, combination LHS' = [ ...LHS, Y ] doesn't contain any new information because it can be derived from LHS.
    // So, if a dependency LHS' => X did hold, the dependency LHS => X would also hold. But we know LHS => X doesn't hold, so LHS' => X can't hold either.
    // Because LHS' => X doesn't hold, LHS' (or its superset) must be a part of M_X, which contradicts our assumption that LHS is an element of M_X.
    //
    // ### We break all FDs that don't hold.
    //
    // Any dependency LHS => X where LHS is a part of M_X is broken by its corresponding row.
    // Any dependency with a smaller LHS is broken by the same row.

    private ArmstrongRelation innerRun() {
        setupColumnProviders();

        final var requiredRows = computeRequiredRows();
        final var prevByLhs = collectPrevRowsByLhs();

        final var nextRows = createExampleRows(requiredRows, prevByLhs);
        nextRows.sort((a, b) -> a.lhsSet.compareTo(b.lhsSet));

        final var referenceRow = getReferenceRow();

        return new ArmstrongRelation(
            referenceRow,
            nextRows
        );
    }

    /**
     * Each returned combination LHS, RHS corresponds to one row (see {@link ExampleRow}) in the final Armstrong relation (except for the reference row).
     * All rows have unique LHS.
     * All values in the LHS columns should be the same as in the reference row, all other should be unique.
     * All RHS columns needs to be evaluated by the user, all other doesn't have to.
     */
    private Map<ColumnSet, ColumnSet> computeRequiredRows() {
        final Map<ColumnSet, ColumnSet> output = new HashMap<>();

        for (final MaxSet maxSet : maxSets.sets()) {
            // For each LHS in the max set, we need to create a row.
            maxSet.elements().forEach(lhsSet -> {
                final var rhsSet = output.computeIfAbsent(lhsSet, x -> ColumnSet.empty());
                // The RHS sets consist of all classes that have the LHS in their max set.
                rhsSet.set(maxSet.forClass);
            });
        }

        return output;
    }

    private Map<ColumnSet, ExampleRow> collectPrevRowsByLhs() {
        final Map<ColumnSet, ExampleRow> prevRows = new HashMap<>();

        if (prev != null) {
            for (final ExampleRow exampleRow : prev.exampleRows)
            prevRows.put(exampleRow.lhsSet, exampleRow);
        }

        return prevRows;
    }

    private List<ExampleRow> createExampleRows(Map<ColumnSet, ColumnSet> requiredRows, Map<ColumnSet, ExampleRow> prevByLhs) {
        final List<ExampleRow> output = new ArrayList<>();
        final var isPositive = prev == null || isEvaluatingPositives;

        for (final var entry : requiredRows.entrySet()) {
            final ColumnSet lhsSet = entry.getKey();
            final ColumnSet rhsSet = entry.getValue();

            final var prevRow = prevByLhs.get(lhsSet);
            if (prevRow != null) {
                // If the row already exists in the previous Armstrong relation, we can just use it.
                // This also keeps its initial positive state (if it's positive).
                // However, its RHS might have changed.
                prevRow.rhsSet = rhsSet;
                output.add(prevRow);
                continue;
            }

            final ExampleRow exampleRow = new ExampleRow(
                generateRowValues(lhsSet),
                lhsSet,
                rhsSet,
                isPositive
            );
            output.add(exampleRow);
        }

        return output;
    }

    private final List<ColumnValueProvider> columnProviders = new ArrayList<>();

    private void setupColumnProviders() {
        for (int i = 0; i < columnsCount; i++) {
            final var referenceValue = prev == null ? null : prev.referenceRow[i];
            final var provider = new ColumnValueProvider(i, dataset.getRows(), referenceValue);
            columnProviders.add(provider);

            if (prev != null) {
                // Load the used values from the previous Armstrong relation. We need the index to be final because of the lambda.
                final int finalI = i;
                final var usedValues = prev.exampleRows.stream()
                    .map(row -> row.values[finalI])
                    .collect(Collectors.toSet());
                provider.loadUsedValues(usedValues);
            }
        }
    }

    private String[] getReferenceRow() {
        final String[] referenceRow = new String[columnsCount];
        for (int i = 0; i < columnsCount; i++)
            referenceRow[i] = columnProviders.get(i).reference();

        return referenceRow;
    }

    /** Make sure to only call this function once on each unique lhs - subsequent calls would result in new values. */
    private String[] generateRowValues(ColumnSet lhsSet) {
        final String[] row = new String[columnProviders.size()];
        for (int i = 0; i < columnProviders.size(); i++) {
            // If the column is part of the LHS set, use the reference value, otherwise generate a new value.
            row[i] = lhsSet.get(i)
                ? columnProviders.get(i).reference()
                : columnProviders.get(i).next();
        }

        return row;
    }

    private class ColumnValueProvider {

        private final int columnIndex;
        private final List<String[]> datasetRows;
        private int indexInDataset = 0;

        /** Value in the reference row. */
        private @Nullable String referenceValue;
        private final Set<String> usedValues = new HashSet<>();
        private int lastGeneratedValue = 0;

        ColumnValueProvider(int columnIndex, List<String[]> datasetRows, @Nullable String referenceValue) {
            this.columnIndex = columnIndex;
            this.datasetRows = datasetRows;
            this.referenceValue = referenceValue;
            if (referenceValue != null)
                usedValues.add(referenceValue);
        }

        void loadUsedValues(Collection<String> values) {
            usedValues.addAll(values);
        }

        String reference() {
            if (referenceValue == null)
                referenceValue = next();

            return referenceValue;
        }

        String next() {
            @Nullable String nextFromDataset = tryGetUniqueDatasetValue();
            return nextFromDataset != null
                ? nextFromDataset
                : generateUniqueValue();
        }

        private @Nullable String tryGetUniqueDatasetValue() {
            while (indexInDataset < datasetRows.size()) {
                final @Nullable String value = datasetRows.get(indexInDataset)[columnIndex];
                indexInDataset++;

                if (value == null || value.isEmpty())
                    continue;

                if (!usedValues.contains(value)) {
                    usedValues.add(value);
                    return value;
                }
            }

            return null;
        }

        private static final String GENERATED_PREFIX = "#";

        private String generateUniqueValue() {
            @Nullable String uniqueValue;
            do {
                uniqueValue = GENERATED_PREFIX + lastGeneratedValue++;
            } while (usedValues.contains(uniqueValue));

            usedValues.add(uniqueValue);

            return uniqueValue;
        }

    }

}
