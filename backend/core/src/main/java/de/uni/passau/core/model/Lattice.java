package de.uni.passau.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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
 * The lattice of column sets for a specific class.
 * Doesn't contain the column sets themselves, but rather just their states.
 */
@JsonSerialize(using = Lattice.Serializer.class)
@JsonDeserialize(using = Lattice.Deserializer.class)
public class Lattice {

    /** How many columns in the whole relation we can have. Larger values aren't allowed because they would lead to too large lattices on FE. */
    public static final int MAX_COLUMNS = 10;

    /** Name of the class column. */
    public String classColumn;
    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /**
     * For each row, contains an array of states (each corresponding to one {@link ColumnSet} from the row).
     * The column sets are in ascending order. Row on index i contains sets of size i + 1.
     */
    public CellType[][] rows;

    public Lattice(String classColumn, String[] columns, CellType[][] rows) {
        this.classColumn = classColumn;
        this.columns = columns;
        this.rows = rows;
    }

    public static Lattice createPlaceholder(String classColumn, String[] columns) {
        return new Lattice(classColumn, columns, new CellType[0][0]);
    }

    public boolean isPlaceholder() {
        return rows.length == 0;
    }

    /**
     * A type of an element from the max set for a class.
     * We use integers to save space when storing the lattice.
     */
    @JsonSerialize(using = CellType.Serializer.class)
    @JsonDeserialize(using = CellType.Deserializer.class)
    public enum CellType {
        GENUINE_FINAL((byte) 0),
        GENUINE_TEMP((byte) 1),
        GENUINE_DERIVED((byte) 2),
        INVALID_FINAL((byte) 3),
        INVALID_TEMP((byte) 4),
        INVALID_DERIVED((byte) 5),
        FAKE_FINAL((byte) 6),
        FAKE_TEMP((byte) 7),
        FAKE_DERIVED((byte) 8);

        public final byte value;

        private CellType(byte value) {
            this.value = value;
        }

        // The custom serialization is probably not needed, as we serialize the whole lattice using much more efficient encoding. However, it might be useful for debugging.

        public static class Serializer extends StdSerializer<CellType> {
            public Serializer() { this(null); }
            public Serializer(Class<CellType> t) { super(t); }

            @Override public void serialize(CellType key, JsonGenerator generator, SerializerProvider provider) throws IOException {
                generator.writeNumber(key.value);
            }
        }

        public static class Deserializer extends StdDeserializer<CellType> {
            public Deserializer() { this(null); }
            public Deserializer(Class<?> vc) { super(vc); }

            @Override public CellType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                final JsonNode node = parser.getCodec().readTree(parser);
                return CellType.values()[node.asInt()];
            }
        }
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<Lattice> {
        public Serializer() { this(null); }
        public Serializer(Class<Lattice> t) { super(t); }

        @Override public void serialize(Lattice lattice, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("classColumn", lattice.classColumn);

            generator.writeFieldName("columns");
            generator.writeArray(lattice.columns, 0, lattice.columns.length);

            final var rows = new String[lattice.rows.length];
            for (int i = 0; i < lattice.rows.length; i++)
                rows[i] = Lattice.cellTypesToBase64String(lattice.rows[i]);

            generator.writeFieldName("rows");
            generator.writeArray(rows, 0, rows.length);

            generator.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<Lattice> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public Lattice deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final String classColumn = node.get("classColumn").asText();

            final List<String> columns = new ArrayList<>();
            for (final JsonNode item : node.get("columns"))
                columns.add(item.asText());

            final List<CellType[]> rows = new ArrayList<>();
            for (final JsonNode item : node.get("candidates"))
                rows.add(cellTypesFromBase64String(item.asText()));

            return new Lattice(classColumn, columns.toArray(String[]::new), rows.toArray(CellType[][]::new));
        }
    }

    private static String cellTypesToBase64String(CellType[] types) {
        final boolean isOdd = types.length % 2 == 1;
        final int evenLength = types.length - (isOdd ? 1 : 0);

        final int evenBytesLength = evenLength / 2;
        final int totalBytesLength = evenBytesLength + (isOdd ? 1 : 0);

        final byte[] bytes = new byte[totalBytesLength];

        for (int i = 0; i < evenBytesLength; i++) {
            final int j = i * 2;
            bytes[i] = (byte) ((types[j].value << 4) | types[j + 1].value);
        }

        if (isOdd)
            // The last half-byte is padded with 1s (so that it is not confused with a valid type).
            bytes[totalBytesLength - 1] = (byte) ((types[types.length - 1].value << 4) | 0x0F);

        return Base64.getEncoder().encodeToString(bytes);
    }

    private static CellType[] cellTypesFromBase64String(String string) {
        final byte[] bytes = Base64.getDecoder().decode(string.getBytes());

        final boolean isOdd = (bytes[bytes.length - 1] & 0x0F) == 0x0F; // Check if the last half-byte is padded with 1s.
        final int evenBytesLength = bytes.length - (isOdd ? 1 : 0);

        final int evenLength = evenBytesLength * 2;
        final int totalLength = evenLength + (isOdd ? 1 : 0);

        final CellType[] types = new CellType[totalLength];

        for (int i = 0; i < evenBytesLength; i++) {
            final int j = i * 2;
            types[j] = CellType.values()[bytes[i] >> 4];
            types[j + 1] = CellType.values()[bytes[i] & 0x0F];
        }

        if (isOdd)
            types[totalLength - 1] = CellType.values()[bytes[bytes.length - 1] >> 4];

        return types;
    }

    // #endregion

}
