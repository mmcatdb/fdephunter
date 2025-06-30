package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeLatticeException;
import de.uni.passau.core.model.Lattice;
import de.uni.passau.core.model.MaxSet;

public class ComputeLattice {

    public static Lattice run(
        MaxSet maxSet,
        MaxSet initialMaxSet
        // TODO More arguments are probably needed.
    ) {
        try {
            final var algorithm = new ComputeLattice(maxSet, initialMaxSet);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeLatticeException.inner(e);
        }
    }

    private final MaxSet maxSet;
    private final MaxSet initialMaxSet;

    private ComputeLattice(MaxSet maxSet, MaxSet initialMaxSet) {
        this.maxSet = maxSet;
        this.initialMaxSet = initialMaxSet;
    }

    private Lattice innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
