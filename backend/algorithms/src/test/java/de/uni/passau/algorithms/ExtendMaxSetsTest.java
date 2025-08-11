package de.uni.passau.algorithms;

import static de.uni.passau.algorithms.TestUtils.parseMaxSets;
import static de.uni.passau.algorithms.TestUtils.parseColumnSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendMaxSetsTest {

    @Test
    void generateCandidateIfSupersetInsteadOfSubset() {
        final var initial = parseMaxSets(
            // The 234 subset is missing, but the 1234 candidate should still be generated, because the superset of 234 is here.
            "123 124 134 234567",
            // This is the most easy "empty" max set. We can't just use an empty string, because truly empty max set would support all fds, thus heavily interfering with other max sets.
            "0234567",
            "0134567",
            "0124567",
            "0123567",
            "0123467",
            "0123457",
            "0123456"
        );

        final var extended = ExtendMaxSets.run(initial, 4);

        final var expectedCandidate = parseColumnSet("1234");
        assertTrue(extended.sets().get(0).hasCandidate(expectedCandidate), "The expected candidate " + expectedCandidate + " was not generated.");
    }

    @Test
    void pruneByOtherClasses() {
        final var initial = parseMaxSets(
            "1 2 3", // 23 will be generated, but because the FD below, it should be replaced by 123.
            "0 2", // 3 is missing - 23 is not going to be generated - 23 => 1 is FD.
            "013",
            "014"
        );

        final var extended = ExtendMaxSets.run(initial, 2);

        System.out.println(extended);
        System.out.println(extended);
    }

}
