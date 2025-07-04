package de.uni.passau.algorithms;

import de.uni.passau.algorithms.fd.FunctionalDependencyGroup;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComputeFDTest {
    
    @Test
    public void testComputeFDEndToEnd() {
        Path path = Paths.get("src", "test", "resources", "iris.csv");
        Dataset dataset = new CSVDataset(path.toString(), false);
        dataset.load();
        Map<Integer, List<FunctionalDependencyGroup>> result = ComputeFD.run(dataset);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Map.Entry<Integer, List<FunctionalDependencyGroup>> entry : result.entrySet()) {
            List<FunctionalDependencyGroup> fds = entry.getValue();
            assertNotNull(fds);
            for (FunctionalDependencyGroup fd : fds) {
                assertNotNull(fd);
                assertTrue(fd.getValues().size() > 0);
            }
        }
        // TODO: Test more precise properties of the result
    }
   
}