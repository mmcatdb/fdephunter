package de.uni.passau.server.approach.service;

import de.uni.passau.core.dataset.csv.CSVDataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OurApproachServiceTest {
    private static final String TEST_FILE_NAME = "test.csv";
    private static final String[] TEST_HEADER = { "column_1", "column_2", "column_3" };
    private static final String[][] TEST_DATA = { { "1", "", "3" }, { "4", "", "6" }, { "", "", "6" } };

    private CSVDataset dataset;

    @BeforeEach
    public void setUp() throws IOException {
        Path testFilePath = Files.createTempFile(TEST_FILE_NAME, "");
        File testFile = testFilePath.toFile();
        testFile.deleteOnExit();
        Files.write(testFilePath, (String.join(",", TEST_HEADER) + "\n").getBytes());
        for (String[] row : TEST_DATA) {
            Files.write(testFilePath, (String.join(",", row) + "\n").getBytes(), java.nio.file.StandardOpenOption.APPEND);
        }
        dataset = new CSVDataset(testFile.getAbsolutePath(), true);
    }


    @Test
    void testNullSemantic() {
        OurApproachService ourApproachService = new OurApproachService();
        
//        List<FDInit> fds = ourApproachService.execute(dataset);
//        Assertions.assertEquals(2, fds.size());
//        Assertions.assertEquals("[0]", fds.get(0).getFirst().toString());
//        Assertions.assertEquals("2", fds.get(0).getSecond());
//        Assertions.assertEquals("[2]", fds.get(1).getFirst().toString());
//        Assertions.assertEquals("0", fds.get(1).getSecond());
    }
}
