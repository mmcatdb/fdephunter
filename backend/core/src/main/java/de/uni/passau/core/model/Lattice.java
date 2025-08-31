package de.uni.passau.core.model;

import java.io.IOException;

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
public class Lattice {

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

    /**
     * A type of an element from the max set for a class.
     * We use integers to save space when storing the lattice.
     */
    @JsonSerialize(using = CellType.Serializer.class)
    @JsonDeserialize(using = CellType.Deserializer.class)
    public enum CellType {
        GENUINE_FINAL(0),
        GENUINE_TEMP(1),
        GENUINE_DERIVED(2),
        INVALID_FINAL(3),
        INVALID_TEMP(4),
        INVALID_DERIVED(5),
        FAKE_FINAL(6),
        FAKE_TEMP(7),
        FAKE_DERIVED(8);

        public final int value;

        private CellType(int value) {
            this.value = value;
        }

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

}
