package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeLatticeException;
import de.uni.passau.core.example.McLattice;

public class ComputeLattice {

    public static McLattice run() {
        try {
            final var algorithm = new ComputeLattice();
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeLatticeException.inner(e);
        }
    }

    private ComputeLattice() {

    }

    private McLattice innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
