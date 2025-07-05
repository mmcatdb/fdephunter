package de.uni.passau.core.dataset.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset.DatasetMetadata;
import de.uni.passau.core.dataset.CSVDataset.CSVDatasetMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CSVDatasetMetadataTest {
    private static final String TEST_FILE_NAME = "test.csv";
    private static String ACTUAL_FILE_NAME = null;
    private static final String[] TEST_HEADER = { "column_1", "column_2", "column_3" };
    private static final String[][] TEST_DATA = { { "1", "2", "3" }, { "4", "5", "6" }, { "7", "8", "9" } };

    private CSVDataset dataset;

    @BeforeEach
    public void setUp() throws IOException {
        final Path testFilePath = Files.createTempFile(TEST_FILE_NAME, "");
        final File testFile = testFilePath.toFile();
        ACTUAL_FILE_NAME = testFile.getName();
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
    void testGetMetadata() {
        dataset.load();
        final DatasetMetadata metadata = dataset.getMetadata();
        assertNotNull(metadata);
        assertTrue(metadata instanceof CSVDatasetMetadata);
        final CSVDatasetMetadata csvMetadata = (CSVDatasetMetadata) metadata;
        assertEquals(ACTUAL_FILE_NAME, csvMetadata.filename());
        assertEquals(true, csvMetadata.hasHeader());
        assertEquals(TEST_DATA.length, csvMetadata.numberOfRows());
        assertEquals(TEST_DATA[0].length, csvMetadata.numberOfColumns());
    }
}
