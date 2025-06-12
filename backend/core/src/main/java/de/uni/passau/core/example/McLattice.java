package de.uni.passau.core.example;

import java.util.Arrays;
import java.util.stream.IntStream;

import de.uni.passau.core.Utils;

/**
 * The lattice of of elements for a specific class. Each element is a set of columns, the class is also a column. Each element then corresponds to an element of the max set for the class (max set of attributes of on the LHS that do not form a functional dependency with the class on the RHS).
 * No, the lattice is not from Scotland, although it could be.
 */
public class McLattice {

    /** Name of the class column. */
    String classColumn;
    /** Names of the columns. They are expected to be unique. */
    String[] columns;
    /**
     * States of the cells in the rows. Each cell max set should be computable form its row and cell index (see {@link #computeColumnIndexesForRow()}).
     * The indexes go from 0 to the amount of columns minus one.
     */
    McType[][] rows;

    /** A type of an element from the max set for a class. */
    enum McType {
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
     * For a given row index, returns an array of sets. Each set is an array of indexes of columns that form the max set for the given cell.
     * Important note: the indexes are indexes of the columns for the class, not for the whole relation. E.g., if all columns are [ A, B, C ] and the class is B, then the returned indexes index to [ A, C ].
     * Warning: Don't use for large lattices - int overflow is certain.
     */
    public int[][] computeColumnIndexesForRow(int rowIndex) {
        // The most bottom row (0) is: [ [0], [1], [2], ... ]
        // The next row (1) is: [ [01], [02], [03], ..., [12], [13], ... ]
        // The last row is: [ [0123...] ]
        // However, there is always one column missing - i.e., if we want the lattice for class "0", then the first row should be [ [1], [2], [3], ... ] instead.
        // To solve this, we simply create a lattice for one less column and then map the columns accordingly. But that's out of the scope of this function.


        // The total number of columns in each cell.
        final int cellSize = rowIndex + 1;
        final int columnCount = columns.length - 1;
        final int rowSize = Utils.factorial(columnCount) / (Utils.factorial(cellSize) * Utils.factorial(columnCount - cellSize));

        final int[][] output = new int[rowSize][];

        // Starts at [ 0, 1, 2 ] (for the 3rd row).
        final int[] indexes = IntStream.range(0, cellSize).toArray();
        // The max values of the indexes. Should be [ 7, 8, 9 ] (for the 3rd row and 10 columns).
        final int[] maxIndexes = IntStream.range(columnCount - cellSize, columnCount).toArray();

        int outputIndex = 0;
        while (indexes[0] <= maxIndexes[0]) {
            output[outputIndex++] = Arrays.copyOf(indexes, cellSize);

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
