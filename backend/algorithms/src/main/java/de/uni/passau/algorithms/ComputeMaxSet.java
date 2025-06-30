package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.core.example.MaxSet;

public class ComputeMaxSet {

    public static MaxSet run() {
        try {
            final var algorithm = new ComputeMaxSet();
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

    private ComputeMaxSet() {

    }

    private MaxSet innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
