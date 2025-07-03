package de.uni.passau.core.model;

import java.io.IOException;
import java.util.Base64;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Iterator;

/**
 * Represents a non-empty set of columns.
 */
@JsonSerialize(using = ColumnSet.Serializer.class)
@JsonDeserialize(using = ColumnSet.Deserializer.class)
public class ColumnSet implements Comparable<ColumnSet> {

    /** Indexes of the columns. */
    private final BitSet columns;

    private ColumnSet(BitSet columns) {
        this.columns = columns;
    }

    public ColumnSet() {
        this.columns = new BitSet();
    }

    public static ColumnSet fromIndexes(int ...columns) {
        if (columns.length == 0)
            throw new IllegalArgumentException("ColumnSet must contain at least one column.");

        final BitSet bitSet = new BitSet();
        for (int column : columns)
            bitSet.set(column);

        return new ColumnSet(bitSet);
    }

    @Override public String toString() {
        return columns.toString();
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

    public BitSet getColumns() {
        return (BitSet) this.columns.clone();
    }

    public void and(ColumnSet other) {
        this.columns.and(other.columns);
    }

    public boolean intersects(ColumnSet other) {
        BitSet copy = (BitSet) this.columns.clone();
        copy.and(other.columns);
        return !copy.isEmpty();
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

    public ColumnSet or(ColumnSet other) {
        BitSet result = (BitSet) this.columns.clone();
        result.or(other.columns);
        return new ColumnSet(result);
    }

    public ColumnSet xor(ColumnSet other) {
        BitSet result = (BitSet) this.columns.clone();
        result.xor(other.columns);
        return new ColumnSet(result);
    }

    public void flip(int columnIndex) {
        this.columns.flip(columnIndex);
    }

    public int length() {
        return this.columns.length();
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

    @Override public int hashCode() {
        return columns.hashCode();
    }

    /**
     * Returns an iterator over the column indices in this set.
     * The indices are returned in ascending order.
     */
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int nextIndex = columns.nextSetBit(0);

            @Override
            public boolean hasNext() {
                return nextIndex != -1;
            }

            @Override
            public Integer next() {
                if (nextIndex == -1) {
                    throw new java.util.NoSuchElementException();
                }
                int current = nextIndex;
                nextIndex = columns.nextSetBit(nextIndex + 1);
                return current;
            }
        };
    }

    public int nextSetBit(int fromIndex) {
        return columns.nextSetBit(fromIndex);
    }

    public int prevSetBit(int fromIndex) {
        return columns.previousSetBit(fromIndex);
    }

    public int getBit(int index) {
        if (index < 0 || index >= columns.length()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + columns.length());
        }
        return columns.get(index) ? 1 : 0;
    }


    // region Serialization

    public String toBase64String() {
        final byte[] bytes = columns.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static ColumnSet fromBase64String(String string) {
        final byte[] bytes = Base64.getDecoder().decode(string.getBytes());
        return new ColumnSet(BitSet.valueOf(bytes));
    }

    public static class Serializer extends StdSerializer<ColumnSet> {
        public Serializer() { this(null); }
        public Serializer(Class<ColumnSet> t) { super(t); }

        @Override public void serialize(ColumnSet set, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(set.toBase64String());
        }
    }

    public static class Deserializer extends StdDeserializer<ColumnSet> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public ColumnSet deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
            return ColumnSet.fromBase64String(node.asText());
        }
    }

    // endregion

}
