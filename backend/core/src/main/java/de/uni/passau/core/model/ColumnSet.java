package de.uni.passau.core.model;

import java.util.BitSet;

import org.apache.commons.lang3.StringUtils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * Represents a non-empty set of columns.
 */
public class ColumnSet implements Comparable<ColumnSet> {

    /** Indexes of the columns. */
    private final BitSet columns;

    private ColumnSet(BitSet columns) {
        this.columns = columns;
    }

    public ColumnSet() {
        this.columns = new BitSet();
    }

    public static ColumnSet fromIndexes(int[] columns) {
        if (columns.length == 0)
            throw new IllegalArgumentException("ColumnSet must contain at least one column.");

        final BitSet bitSet = new BitSet();
        for (int column : columns)
            bitSet.set(column);

        return new ColumnSet(bitSet);
    }

    @Override public String toString() {
        return "(" + StringUtils.join(columns, ',') + ")";
    }

    public LongList convertToLongList() {
		LongList bits = new LongArrayList();
		long lastIndex = columns.nextSetBit(0);
		while (lastIndex != -1) {
			bits.add(lastIndex);
            // TODO: This defeats the purpose of the LongList. Do we really need it or are ints enough?
			lastIndex = columns.nextSetBit((int) (lastIndex + 1));
		}
		return bits;
	}

	public IntList convertToIntList() {
		IntList bits = new IntArrayList();
		int lastIndex = columns.nextSetBit(0);
		while (lastIndex != -1) {
			bits.add(lastIndex);
			lastIndex = columns.nextSetBit(lastIndex + 1);
		}
		return bits;
	}

    public int size() {
        return columns.cardinality();
    }

    @Override public ColumnSet clone() {
        return new ColumnSet((BitSet) this.columns.clone());
    }

    public void and(ColumnSet other) {
        this.columns.and(other.columns);
    }

    public void andNot(ColumnSet other) {
        this.columns.andNot(other.columns);
    }

    public boolean isEmpty() {
        return this.columns.isEmpty();
    }

    public boolean isSuperSetOf(ColumnSet other) {
        BitSet copy = (BitSet) other.columns.clone();
        copy.andNot(this.columns);
        return copy.isEmpty();
    }

    public void set(int columnIndex) {
        this.columns.set(columnIndex);
    }

    public void set(int columnIndex, int value) {
        this.columns.set(columnIndex, value);
    }

    public boolean get(int columnIndex) {
        return this.columns.get(columnIndex);
    }

    public ColumnSet xor(ColumnSet other) {
        BitSet result = (BitSet) this.columns.clone();
        result.xor(other.columns);
        return new ColumnSet(result);
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

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ColumnSet other = (ColumnSet) obj;
        return columns.equals(other.columns);
    }

    @Override
    public int hashCode() {
        return columns.hashCode();
    }

}
