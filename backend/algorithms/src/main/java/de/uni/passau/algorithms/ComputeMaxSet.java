package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.MaxSet;

public class ComputeMaxSet {

    public static MaxSet run(
        /** The initial relation. */
        Dataset dataset
    ) {
        try {
            final var algorithm = new ComputeMaxSet(dataset);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

    private final Dataset dataset;

    private ComputeMaxSet(Dataset dataset) {
        this.dataset = dataset;
    }

    private MaxSet innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
