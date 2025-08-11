package de.uni.passau.algorithms;

import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.MaxSet;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComputeMaxSetTest {

    @Test
    void testComputeMaxSetEndToEnd() {
        Path path = Paths.get("src", "test", "resources", "iris.csv");
        Dataset dataset = new CSVDataset(path.toString(), false, ',');
        dataset.load();
        List<MaxSet> result = ComputeMaxSets.run(dataset).sets();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(maxSet -> maxSet.confirmedCount() > 0));
        // TODO: Test more precise properties of the result
    }

}
