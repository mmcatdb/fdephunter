package de.uni.passau.core.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a non-empty set of columns.
 */
public class ColumnSet implements Comparable<ColumnSet> {

    /**
     * Ordered indexes of the column indexes.
     * It's a set, although it's represented as an array.
     */
    public final int[] columns;

    private ColumnSet(int[] columns) {
        this.columns = columns;
    }

    public static ColumnSet fromSorted(int[] columns) {
        final int[] copy = copyAndCheckInputColumns(columns, false);
        return new ColumnSet(copy);
    }

    public static ColumnSet fromUnsorted(int[] unsortedColumns) {
        final int[] copy = copyAndCheckInputColumns(unsortedColumns, true);
        return new ColumnSet(copy);
    }

    private static int[] copyAndCheckInputColumns(int[] input, boolean doSort) {
        if (input.length == 0)
            throw new IllegalArgumentException("ColumnSet must contain at least one column.");

        final int[] output = Arrays.copyOf(input, input.length);

        if (doSort)
            java.util.Arrays.sort(output);

        // Check if the columns are sorted or not unique.
        for (int i = 0; i < output.length - 1; i++) {
            if (output[i] >= output[i + 1])
                throw new IllegalArgumentException("ColumnSet columns must be sorted in ascending order.");
        }

        return output;
    }

    @Override public int compareTo(ColumnSet o) {
        if (this.columns.length != o.columns.length)
            return this.columns.length - o.columns.length;

        for (int i = 0; i < this.columns.length; i++) {
            if (this.columns[i] != o.columns[i])
                return this.columns[i] - o.columns[i];
        }

        return 0;
    }

    @Override public String toString() {
        return "(" + StringUtils.join(columns, ',') + ")";
    }

}
