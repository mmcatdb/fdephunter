package de.uni.passau.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Represents a non-empty set of columns.
 */
@JsonSerialize(using = ColumnSet.Serializer.class)
@JsonDeserialize(using = ColumnSet.Deserializer.class)
public class ColumnSet implements Comparable<ColumnSet> {

    /** Indexes of the columns. */
    private final BitSet columns;

    protected ColumnSet() {
        this.columns = new BitSet();
    }

    private ColumnSet(BitSet columns) {
        this.columns = columns;
    }

    public static ColumnSet empty() {
        return new ColumnSet();
    }

    public static ColumnSet fromIndex(int column) {
        final BitSet bitSet = new BitSet();
        bitSet.set(column);

        return new ColumnSet(bitSet);
    }

    public static ColumnSet fromIndexes(int ...columns) {
        final BitSet bitSet = new BitSet();
        for (int column : columns)
            bitSet.set(column);

        return new ColumnSet(bitSet);
    }

    public static ColumnSet fromRange(int start, int end) {
        // No need to check the bounds, as BitSet will handle it for us.
        final BitSet bitSet = new BitSet();
        bitSet.set(start, end);

        return new ColumnSet(bitSet);
    }

    @Override public String toString() {
        return columns.toString();
    }

    /**
     * Returns all indexes of the columns in this set in ascending order.
     */
    public int[] toIndexes() {
        final int size = columns.cardinality();
        final int[] output = new int[size];

        int value = -1;
        for (int i = 0; i < size; i++) {
            value = columns.nextSetBit(value + 1);
            output[i] = value;
        }

        return output;
    }

    public int size() {
        return columns.cardinality();
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public boolean intersects(ColumnSet other) {
        BitSet copy = (BitSet) columns.clone();
        copy.and(other.columns);
        return !copy.isEmpty();
    }

    public boolean isSupersetOf(ColumnSet other) {
        BitSet copy = (BitSet) other.columns.clone();
        copy.andNot(columns);
        return copy.isEmpty();
    }

    public int lastIndex() {
        return columns.length() - 1;
    }

    public boolean get(int index) {
        return columns.get(index);
    }

    public void set(int index) {
        columns.set(index);
    }

    public void set(int fromIndex, int toIndex) {
        columns.set(fromIndex, toIndex);
    }

    public void clear(int index) {
        columns.clear(index);
    }

    public void flip(int index) {
        columns.flip(index);
    }

    @Override public ColumnSet clone() {
        return new ColumnSet((BitSet) columns.clone());
    }

    public void and(ColumnSet other) {
        columns.and(other.columns);
    }

    public void andNot(ColumnSet other) {
        columns.andNot(other.columns);
    }

    public void or(ColumnSet other) {
        columns.or(other.columns);
    }

    public void xor(ColumnSet other) {
        columns.xor(other.columns);
    }

    /** Returns new column set with the given size that has all bits inverted (if the bit isn't defined in the initial set, it's considered 0). */
    public ColumnSet toInverse(int size) {
        final BitSet output = new BitSet(size);
        output.set(0, size);
        output.xor(columns);

        return new ColumnSet(output);
    }

    /** Returns all one-large sets (we try to extend the set by columns with index less than <code>size</code> */
    public List<ColumnSet> toSupersets(int size) {
        final List<ColumnSet> output = new ArrayList<>();

        for (final var index : toInverse(size).toIndexes()) {
            final var superset = clone();
            superset.set(index);
            output.add(superset);
        }

        return output;
    }

    /** Returns all one-smaller sets. */
    public List<ColumnSet> toSubsets() {
        final List<ColumnSet> output = new ArrayList<>();

        for (final var index : toIndexes()) {
            final var subset = clone();
            subset.clear(index);
            output.add(subset);
        }

        return output;
    }

    /**
     * In the class representation, the index of the class is skipped (and >= indexes are shifted).
     * E.g., if the columns are {0,1,2,4} (in the UR) and the class is 2, then they will be {0,1,3,5} (in the CR).
     */
    public ColumnSet toClassRepresentation(int forClass) {
        final BitSet output = (BitSet) columns.clone();

        // Either forClass, or the first set index after forClass, or -1 if there is none.
        int index = columns.nextSetBit(forClass);
        if (index == -1)
            return new ColumnSet(output);

        output.clear(index, output.length());
        while (index != -1) {
            output.set(index + 1);
            index = columns.nextSetBit(index + 1);
        }

        return new ColumnSet(output);
    }

    /**
     * Reverse of {@link #toClassRepresentation(int)}.
     */
    public ColumnSet toUniversalRepresentation(int forClass) {
        final BitSet output = (BitSet) columns.clone();

        // There can be no index at forClass - everything on that index or after is shifted up by one.
        int index = columns.nextSetBit(forClass + 1);
        if (index == -1)
            return new ColumnSet(output);

        output.clear(index, output.length());
        while (index != -1) {
            output.set(index - 1);
            index = columns.nextSetBit(index + 1);
        }

        return new ColumnSet(output);
    }

    @Override public int compareTo(ColumnSet other) {
        final var lengthComparison = columns.cardinality() - other.columns.cardinality();
        if (lengthComparison != 0)
            return lengthComparison;

        final BitSet xor = (BitSet) columns.clone();
        xor.xor(other.columns);

        final int firstDifferent = xor.nextSetBit(0);
        if (firstDifferent == -1)
            return 0;

        return other.columns.get(firstDifferent) ? 1 : -1;
    }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;

        return other instanceof ColumnSet columnSet && columns.equals(columnSet.columns);
    }

    @Override public int hashCode() {
        return columns.hashCode();
    }

    // #region Serialization

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

    // #endregion

}
