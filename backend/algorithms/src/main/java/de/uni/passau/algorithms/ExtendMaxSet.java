package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.MaxSets;

public class ExtendMaxSet {

    public static MaxSets run(
        MaxSets maxSets,
        int lhsSize
    ) {
        try {
            final var algorithm = new ExtendMaxSet(maxSets, lhsSize);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ExtendMaxSetException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final int lhsSize;

    private ExtendMaxSet(MaxSets maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;
    }

    private MaxSets innerRun() throws Exception {
        throw new UnsupportedOperationException("ExtendMaxSet is not implemented yet.");
    }

}
//
