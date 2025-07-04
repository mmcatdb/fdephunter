package de.uni.passau.algorithms;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.FdSet.Fd;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ComputeFDTest {

    @Test
    public void testComputeFDEndToEnd() {
        Path path = Paths.get("src", "test", "resources", "iris.csv");
        Dataset dataset = new CSVDataset(path.toString(), false);
        dataset.load();

        final var maxSets = ComputeMaxSet.run(dataset);
        final FdSet result = ComputeFD.run(maxSets, dataset.getHeader());

        assertNotNull(result);
        assertFalse(result.fds.isEmpty());

        for (Fd fd : result.fds) {
            assertNotNull(fd);
            assertTrue(fd.lhs().size() > 0, "LHS of FD should not be empty");
            assertTrue(fd.rhs().size() > 0, "RHS of FD should not be empty");
            assertFalse(fd.lhs().intersects(fd.rhs()), "LHS and RHS of FD should be disjoint");
        }
        // TODO: Test more precise properties of the result
    }

}
