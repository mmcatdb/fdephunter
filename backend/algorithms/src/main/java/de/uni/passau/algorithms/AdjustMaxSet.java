package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.AdjustMaxSetException;
import de.uni.passau.core.example.MaxSet;

public class AdjustMaxSet {

    public static MaxSet run() {
        try {
            final var algorithm = new AdjustMaxSet();
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private AdjustMaxSet() {

    }

    private MaxSet innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
