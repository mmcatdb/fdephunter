package de.uni.passau.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The lattice of column sets for a specific class.
 * Doesn't contain the column sets themselves, but rather just their states.
 */
public class Lattice {

    /** Name of the class column. */
    public String classColumn;
    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /**
     * For each row, contains a list of states (each corresponding to one {@link ColumnSet} from the row).
     * The column sets are in ascending order, computable by {@link #computeColumnSetsForRow(int)}.
     */
    public List<List<CellType>> rows;

    public Lattice(String classColumn, String[] columns, List<List<CellType>> rows) {
        this.classColumn = classColumn;
        this.columns = columns;
        this.rows = rows;
    }

    /** A type of an element from the max set for a class. */
    enum CellType {
        FINAL,
        INITIAL,
        SUBSET,
        GENUINE,
        CANDIDATE,
        DERIVED,
        ELIMINATED,
        TARGETED,
        COINCIDENTAL,
    }

    /**
     * For a given row index, returns a set of column sets. Each set is an array of indexes of columns in the given cell.
     * Important note: the indexes are indexes of the columns for the class, not for the whole relation. E.g., if all columns are [ A, B, C ] and the class is B, then the returned indexes point to [ A, C ].
     * Warning: Don't use for large lattices - int overflow is certain.
     */
    public List<ColumnSet> computeColumnSetsForRow(int rowIndex) {
        // The most bottom row (0) is: [ [0], [1], [2], ... ]
        // The next row (1) is: [ [01], [02], [03], ..., [12], [13], ... ]
        // The last row is: [ [0123...] ]
        // However, there is always one column missing - i.e., if we want the lattice for class "0", then the first row should be [ [1], [2], [3], ... ] instead.
        // To solve this, we simply create a lattice for one less column and then map the columns accordingly. But that's out of the scope of this function.

        // The total number of columns in each cell.
        final int cellSize = rowIndex + 1;
        final int columnCount = columns.length - 1;

        // The total size is of the row will be this:
        // final int rowSize = Utils.factorial(columnCount) / (Utils.factorial(cellSize) * Utils.factorial(columnCount - cellSize));

        final List<ColumnSet> output = new ArrayList<>();

        // Starts at [ 0, 1, 2 ] (for the 3rd row).
        final int[] indexes = IntStream.range(0, cellSize).toArray();
        // The max values of the indexes. Should be [ 7, 8, 9 ] (for the 3rd row and 10 columns).
        final int[] maxIndexes = IntStream.range(columnCount - cellSize, columnCount).toArray();

        while (indexes[0] <= maxIndexes[0]) {
            output.add(ColumnSet.fromIndexes(indexes));

            int position = cellSize - 1;
            indexes[position]++;

            if (indexes[position] <= maxIndexes[position])
                continue;

            while (position > 0 && indexes[position] >= maxIndexes[position])
                position--;

            indexes[position]++;

            while (position < cellSize - 1) {
                position++;
                indexes[position] = indexes[position - 1] + 1;
            }
        }

        return output;
    }

}
