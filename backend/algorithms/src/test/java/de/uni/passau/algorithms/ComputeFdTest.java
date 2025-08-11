package de.uni.passau.algorithms;

import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.FdSet.Fd;

import static de.uni.passau.algorithms.TestUtils.parseFds;
import static de.uni.passau.algorithms.TestUtils.parseMaxSets;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ComputeFdTest {

    @Test
    void testComputeFdsEndToEnd() {
        final Path path = Paths.get("src", "test", "resources", "iris.csv");
        final Dataset dataset = new CSVDataset(path.toString(), false, ',');
        dataset.load();

        final var maxSets = ComputeMaxSets.run(dataset);
        final FdSet result = ComputeFds.run(maxSets, dataset.getHeader());

        assertNotNull(result);
        assertFalse(result.fds.isEmpty());

        for (final Fd fd : result.fds) {
            assertNotNull(fd);
            assertTrue(fd.lhs().size() > 0, "LHS of FD should not be empty");
            assertTrue(fd.rhs().size() > 0, "RHS of FD should not be empty");
            assertFalse(fd.lhs().intersects(fd.rhs()), "LHS and RHS of FD should be disjoint");
        }
        // TODO: Test more precise properties of the result
    }

    @Test
    void finalFdsFromPaperSucceeds() {
        final MaxSets maxSets = parseMaxSets(
            "1234",
            "234",
            "134",
            "124",
            "12 13 23"
        );

        final FdSet expectedFds = parseFds(5,
            "0 -> 1234",
            "123 -> 4"
        );

        final FdSet actualFds = ComputeFds.run(maxSets, "01234".split(""));

        final String expected = expectedFds.toComparableString();
        final String actual = actualFds.toComparableString();

        assertEquals(expected, actual, "The actual FDs do not match the expected FDs.");
    }

}
