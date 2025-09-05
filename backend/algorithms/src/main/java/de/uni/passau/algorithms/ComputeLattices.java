package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.uni.passau.algorithms.exception.ComputeLatticeException;
import de.uni.passau.core.Utils.CombinationIndexer;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.Lattice;
import de.uni.passau.core.model.Lattices;
import de.uni.passau.core.model.Lattice.CellType;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

public class ComputeLattices {

    public static Lattices run(
        /** In the universal representation */
        String[] allColumns,
        MaxSets maxSets,
        MaxSets initialMaxSets,
        boolean isEvaluatingPositives,
        int lhsSize
    ) {
        try {
            final var algorithm = new ComputeLattices(allColumns, isEvaluatingPositives, lhsSize);
            return algorithm.innerRun(maxSets, initialMaxSets);
        }
        catch (final Exception e) {
            throw ComputeLatticeException.inner(e);
        }
    }

    private final String[] allColumns;
    private final boolean isEvaluatingPositives;
    private final int lhsSize;

    /**
     * Used to compute indexes (in lattice rows) of column sets.
     * The {@link CombinationIndexer#n} attribute determines the number of columns of the largest possible max set element. I.e., the number of column of the original relation minus one. Also the number of columns in the lattice.
     */
    private final CombinationIndexer indexer;

    private ComputeLattices(String[] allColumns, boolean isEvaluatingPositives, int lhsSize) {
        this.allColumns = allColumns;
        this.isEvaluatingPositives = isEvaluatingPositives;
        this.lhsSize = lhsSize;

        indexer = CombinationIndexer.create(allColumns.length - 1);
    }

    private Lattices innerRun(MaxSets maxSets, MaxSets initialMaxSets) {
        final List<Lattice> lattices = new ArrayList<>();

        for (int i = 0; i < maxSets.sets().size(); i++) {
            final MaxSet maxSet = maxSets.sets().get(i);
            final MaxSet initialMaxSet = initialMaxSets.sets().get(i);
            final var lattice = computeLattice(maxSet, initialMaxSet);
            lattices.add(lattice);
        }

        return new Lattices(lattices);
    }

    private CellType[][] lattice;

    private Lattice computeLattice(MaxSet maxSet, MaxSet initialMaxSet) {
        final String className = allColumns[maxSet.forClass];
        final var columns = new String[indexer.n];
        for (int i = 0, j = 0; i < allColumns.length; i++) {
            if (i != maxSet.forClass)
                columns[j++] = allColumns[i];
        }

        if (allColumns.length > Lattice.MAX_COLUMNS)
            return Lattice.createPlaceholder(className, columns);

        // The lattice is small enough, let's compute the rows.

        computeCommonObjects(maxSet, initialMaxSet);

        lattice = createEmptyLattice();

        addNonFds();

        addGenuineFds();

        // Final check.
        for (final var row : lattice) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null)
                    throw new IllegalStateException("Lattice contains null elements after computation.");
            }
        }

        return new Lattice(className, columns, lattice);
    }

    // Common objects used in the computation. They are all in the universal representation.
    private List<ColumnSet> universalElements;
    /** Index i contains elements for row i (i.e., with size i + 1). */
    private List<List<ColumnSet>> universalElementsByRow;
    private Set<ColumnSet> initialElements;

    private void computeCommonObjects(MaxSet maxSet, MaxSet initialMaxSet) {
        this.universalElements = maxSet.elements().map(e -> e.toUniversalRepresentation(maxSet.forClass)).toList();
        this.universalElementsByRow = sortElementsBySize(this.universalElements, indexer.n);
        this.initialElements = initialMaxSet.elements().map(e -> e.toUniversalRepresentation(initialMaxSet.forClass)).collect(Collectors.toSet());
    }

    private CellType[][] createEmptyLattice() {
        final CellType[][] output = new CellType[indexer.n][];

        for (int i = 0; i < indexer.n; i++) {
            final int rowSize = indexer.getNumberOfCombinations(i + 1);
            output[i] = new CellType[rowSize];

            for (int j = 0; j < rowSize; j++)
                output[i][j] = null;
        }

        return output;
    }

    private void addNonFds() {
        // Any max set element that is also in the initial max set invalid - either final or temp.
        // Final is once we have processed its row during the positive examples phase.

        // If it isn't in the initial max set, it's fake - again, either final or temp.
        // Final is once we have processed its row during the negative examples phase. But that's pretty rare, because we expand the max set elements during each iteration.

        // Any element derived from an invalid element is also invalid.
        // Similarly, any element derived from a fake element is also fake (unless it was already marked as invalid).

        // At step i, we want to fill lattice row on index i (by elements with size i + 1).
        for (int rowIndex = indexer.n - 1; rowIndex >= 0; rowIndex--) {
            final CellType[] currentRow = lattice[rowIndex];
            final int prevRowIndex = rowIndex + 1;

            // Derive non-FDs first. The top row is special.
            if (prevRowIndex < indexer.n) {
                final CellType[] prevRow = lattice[prevRowIndex];
                final ColumnSet[] prevRowSets = computeColumnSetsForRow(prevRowIndex);

                for (int i = 0; i < prevRow.length; i++) {
                    final var prevType = prevRow[i];
                    if (prevType == null)
                        continue;

                    final var prevSet = prevRowSets[i];

                    switch (prevType) {
                        case INVALID_DERIVED:
                        case INVALID_FINAL:
                        case INVALID_TEMP:
                            // The element is an invalid FD, so it's descendants are invalid FDs. No need to check nullness here, because invalid FDs are always added first.
                            for (final var subset : prevSet.toSubsets())
                                currentRow[indexer.getIndex(subset)] = CellType.INVALID_DERIVED;
                            break;
                        case FAKE_DERIVED:
                        case FAKE_FINAL:
                        case FAKE_TEMP:
                            // The element is a fake FD, so it's descendants are fake FDs unless they were already marked as invalid.
                            for (final var subset : prevSet.toSubsets()) {
                                if (currentRow[indexer.getIndex(subset)] == null)
                                    currentRow[indexer.getIndex(subset)] = CellType.FAKE_DERIVED;
                            }
                            break;
                        default:
                            // The element is not a non-FD, so it's descendants can't be non-FDs.
                            break;
                    }
                }
            }

            // Then add all non-derived non-FDs.
            final var invalidType = isEvaluatingPositives && rowIndex + 1 > lhsSize
                ? CellType.INVALID_FINAL
                : CellType.INVALID_TEMP;
            final var fakeType = isEvaluatingPositives || rowIndex + 1 < lhsSize
                ? CellType.FAKE_FINAL
                : CellType.FAKE_TEMP;

            for (final var element : universalElementsByRow.get(rowIndex)) {
                final int cellIndex = indexer.getIndex(element);
                if (currentRow[cellIndex] == null)
                    currentRow[cellIndex] = initialElements.contains(element) ? invalidType : fakeType;
            }
        }
    }

    private void addGenuineFds() {
        // Finally, we add genuine FDs.
        // Everything that is neither invalid nor fake is genuine.
        // At step i, we want to fill lattice row on index i (by elements with size i + 1).
        // We take all column sets in the lattice with size i, extend them, and mark them as derived FDs.
        // Then we take all nullelements with size i + 1 and mark them as final/candidate FDs.
        for (int rowIndex = 0; rowIndex < indexer.n; rowIndex++) {
            final CellType[] currentRow = lattice[rowIndex];
            final int prevRowIndex = rowIndex - 1;

            // Derive FDs first. The bottom row is special.
            if (prevRowIndex >= 0) {
                final CellType[] prevRow = lattice[prevRowIndex];
                final ColumnSet[] prevRowSets = computeColumnSetsForRow(prevRowIndex);

                for (int i = 0; i < prevRow.length; i++) {
                    final var prevType = prevRow[i];
                    if (prevType != CellType.GENUINE_DERIVED && prevType != CellType.GENUINE_FINAL && prevType != CellType.GENUINE_TEMP) {
                        // The element is not a genuine FD, so it's descendants can't be derived FDs.
                        continue;
                    }

                    final var prevSet = prevRowSets[i];
                    // These bad boys are new derived FDs!
                    for (final var extendedSet : prevSet.toSupersets(indexer.n))
                        currentRow[indexer.getIndex(extendedSet)] = CellType.GENUINE_DERIVED;
                }
            }

            // Then add all non-derived FDs.
            final var isFinal = isEvaluatingPositives || rowIndex + 1 < lhsSize;
            final var genuineType = isFinal ? CellType.GENUINE_FINAL : CellType.GENUINE_TEMP;

            for (int i = 0; i < currentRow.length; i++) {
                if (currentRow[i] == null)
                    currentRow[i] = genuineType;
            }
        }
    }

    /**
     * Returns list with <code>numberOfColumns</code> lists. List on index i contanins columns sets of size i + 1.
     */
    private static List<List<ColumnSet>> sortElementsBySize(List<ColumnSet> elements, int numberOfColumns) {
        final List<List<ColumnSet>> output = new ArrayList<>();

        for (int i = 0; i < numberOfColumns; i++)
            output.add(new ArrayList<>());

        elements.stream().forEach(element -> output.get(element.size() - 1).add(element));

        return output;
    }

    /**
     * For a given row index, returns an array of column sets. For row index i, all returned column sets have size i + 1.
     * The column sets are in the universal representation and sorted in ascending order.
     * Warning: Don't use for large lattices - int overflow is certain.
     */
    private ColumnSet[] computeColumnSetsForRow(int rowIndex) {
        // The most bottom row (0) is: [ [0], [1], [2], ... ]
        // The next row (1) is: [ [01], [02], [03], ..., [12], [13], ... ]
        // The last row is: [ [0123...] ]

        // The total number of columns in each cell.
        final int cellSize = rowIndex + 1;

        final ColumnSet[] output = new ColumnSet[indexer.getNumberOfCombinations(cellSize)];

        // Starts at [ 0, 1, 2 ] (for the 3rd row).
        final int[] indexes = IntStream.range(0, cellSize).toArray();
        // The max values of the indexes. Should be [ 7, 8, 9 ] (for the 3rd row and 10 columns).
        final int[] maxIndexes = IntStream.range(indexer.n - cellSize, indexer.n).toArray();

        for (int i = 0; i < output.length; i++) {
            output[i] = ColumnSet.fromIndexes(indexes);

            int position = rowIndex;
            indexes[position]++;

            if (indexes[position] <= maxIndexes[position])
                continue;

            while (position > 0 && indexes[position] >= maxIndexes[position])
                position--;

            indexes[position]++;

            while (position < rowIndex) {
                position++;
                indexes[position] = indexes[position - 1] + 1;
            }
        }

        return output;
    }

}
