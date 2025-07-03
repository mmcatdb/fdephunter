package de.uni.passau.algorithms;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.model.MaxSet;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComputeFDTest {
    
    @Test
    public void testComputeFDEndToEnd() {
        Path path = Paths.get("src", "test", "resources", "iris.csv");
        Dataset dataset = new CSVDataset(path.toString(), false);
        dataset.load();
        List<MaxSet> result = ComputeFD.run(dataset);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(maxSet -> maxSet.getCombinations().size() > 0));
        // TODO: Test more precise properties of the result
    }
   
}