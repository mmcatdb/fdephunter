package de.uni.passau.core.model;

import java.util.BitSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a non-empty set of columns.
 */
public class ColumnSet implements Comparable<ColumnSet> {

    /** Indexes of the columns. */
    public final BitSet columns;

    private ColumnSet(BitSet columns) {
        this.columns = columns;
    }

    public static ColumnSet fromIndexes(int[] columns) {
        if (columns.length == 0)
            throw new IllegalArgumentException("ColumnSet must contain at least one column.");

        final BitSet bitSet = new BitSet();
        for (int column : columns)
            bitSet.set(column);

        return new ColumnSet(bitSet);
    }

    @Override public int compareTo(ColumnSet other) {
        if (this.columns.size() != other.columns.size())
            return this.columns.size() - other.columns.size();

        final BitSet xor = (BitSet) columns.clone();
        xor.xor(other.columns);

        final int firstDifferent = xor.length() - 1;
        if(firstDifferent == -1)
            return 0;

        return other.columns.get(firstDifferent) ? 1 : -1;
    }

    @Override public String toString() {
        return "(" + StringUtils.join(columns, ',') + ")";
    }

}
