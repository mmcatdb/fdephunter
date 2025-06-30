package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeARException;
import de.uni.passau.core.example.ArmstrongRelation;

public class ComputeAR {

    public static ArmstrongRelation run() {
        try {
            final var algorithm = new ComputeAR();
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeARException.inner(e);
        }
    }

    private ComputeAR() {

    }

    private ArmstrongRelation innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
