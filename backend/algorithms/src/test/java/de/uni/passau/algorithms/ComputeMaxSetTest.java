package de.uni.passau.algorithms;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.model.MaxSet;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComputeMaxSetTest {
    
    @Test
    void testComputeMaxSetEndToEnd() {
        System.out.println("Running end-to-end test for ComputeMaxSet...");
        Path path = Paths.get("src", "test", "resources", "iris.csv");
        Dataset dataset = new CSVDataset(path.toString(), false);
        System.err.println("Loading dataset from: " + path.toAbsolutePath());
        dataset.load();
        List<MaxSet> result = ComputeMaxSet.run(dataset);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(maxSet -> maxSet.getCombinations().size() > 0));
        // TODO: Test more precise properties of the result
    }
   
}