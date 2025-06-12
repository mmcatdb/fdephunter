package de.uni.passau.core.nex;

import static org.junit.jupiter.api.Assertions.*;

import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.example.NegativeExampleBuilder;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NegativeExampleBuilderTest {
    private NegativeExampleBuilder builder;
    private List<String[]> relation;

    @BeforeEach
    public void setUp() {
        final var dataset = new CSVDataset("iris.csv", true);
        dataset.load();
        builder = new NegativeExampleBuilder(dataset);
        relation = dataset.getRows();
    }

    @Test
    public void testBuildNegativeExample() {
//        NegativeExample example = builder.buildNegativeExample(1, 2);
//        assertNotNull(example);
//        assertEquals("UNANSWERED", example.decision.getStatus().toString());
//        assertEquals("[]", example.decision.getPredefinedReasons().toString());
    }

    @Test
    public void testUniqueValues() {
//        int columnToChange = 2;
//        int row = 3;
//        String[] columns = relation.get(0);
//        NegativeExample example = builder.buildNegativeExample(row, columnToChange);
//        for (int i = 0; i < columns.length; i++) {
//            if (i != columnToChange) {
//                assertEquals(relation.get(row)[i], example.getValues().get(columns[i]));
//            } else {
//                assertNotEquals(relation.get(row)[i], example.getValues().get(columns[i]));
//            }
//        }
    }

}
