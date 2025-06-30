package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeARException;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.model.MaxSet;

/**
 * Computes Armstrong relation for functional dependencies defined by a maximal set.
 * An original relation is needed to provide values for the generated relation, but it's not used to determine the functional dependencies.
 */
public class ComputeAR {

    public static ArmstrongRelation run(
        /** Represents the actual functional dependencies. */
        MaxSet maxSet,
        /** The initial relation. */
        Dataset dataset
    ) {
        try {
            final var algorithm = new ComputeAR(maxSet, dataset);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeARException.inner(e);
        }
    }

    private final MaxSet maxSet;
    private final Dataset dataset;

    private ComputeAR(MaxSet maxSet, Dataset dataset) {
        this.maxSet = maxSet;
        this.dataset = dataset;
    }

    private ArmstrongRelation innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
