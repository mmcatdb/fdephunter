package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.MaxSet;

public class ExtendMaxSet {

    public static List<MaxSet> run(
        List<MaxSet> maxSets,
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

    private final List<MaxSet> maxSets;
    private final int lhsSize;

    private ExtendMaxSet(List<MaxSet> maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;
    }

    private List<MaxSet> innerRun() throws Exception {
        throw new UnsupportedOperationException("ExtendMaxSet is not implemented yet.");
    }

}
//
