package de.uni.passau.core.dataset.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.uni.passau.core.dataset.CSVDataset;

class CSVDatasetHeaderTest {
    private static final String TEST_FILE_NAME = "test.csv";
    private static final String[] TEST_HEADER = { "column_1", "column_2", "column_3" };
    private static final String[][] TEST_DATA = { { "1", "2", "3" }, { "4", "5", "6" }, { "7", "8", "9" } };

    private CSVDataset dataset;

    @BeforeEach
    public void setUp() throws IOException {
        final Path testFilePath = Files.createTempFile(TEST_FILE_NAME, "");
        final File testFile = testFilePath.toFile();
        testFile.deleteOnExit();
        Files.write(testFilePath, (String.join(",", TEST_HEADER) + "\n").getBytes());
        for (String[] row : TEST_DATA)
            Files.write(testFilePath, (String.join(",", row) + "\n").getBytes(), java.nio.file.StandardOpenOption.APPEND);

        dataset = new CSVDataset(testFile.getAbsolutePath(), true);
    }

    @AfterEach
    public void tearDown() {
        dataset.free();
    }

    @Test
    void testGetRows() {
        dataset.load();
        final List<String[]> rows = dataset.getRows();
        assertNotNull(rows);
        assertEquals(TEST_DATA.length, rows.size());
        for (int i = 0; i < TEST_DATA.length; i++) {
            assertArrayEquals(TEST_DATA[i], rows.get(i));
        }
    }

    @Test
    void testGetHeader() {
        dataset.load();
        final String[] header = dataset.getHeader();
        assertNotNull(header);
        assertEquals(TEST_HEADER.length, header.length);
        assertArrayEquals(TEST_HEADER, header);
    }

    @Test
    void testLoad() {
        dataset.load();
        final List<String[]> rows = dataset.getRows();
        assertNotNull(rows);
        assertEquals(TEST_DATA.length, rows.size());
        for (int i = 0; i < TEST_DATA.length; i++) {
            assertArrayEquals(TEST_DATA[i], rows.get(i));
        }
        final String[] header = dataset.getHeader();
        assertNotNull(header);
        assertEquals(TEST_HEADER.length, header.length);
        assertArrayEquals(TEST_HEADER, header);
    }

    @Test
    void testFree() {
        dataset.load();
        dataset.free();
        assertNull(dataset.getRows());
        assertNull(dataset.getHeader());
    }
}
