package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.AdjustMaxSetException;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.MaxSet;

/**
 * Compute new maximal set based on the previous one and expert's decisions.
 */
public class AdjustMaxSet {

    public static MaxSet run(
        /** The previous version of the max set. */
        MaxSet prev,
        /** Example rows, preferably with the expert's decisions. */
        List<ExampleRow> exampleRows
    ) {
        try {
            final var algorithm = new AdjustMaxSet(prev, exampleRows);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw AdjustMaxSetException.inner(e);
        }
    }

    private final MaxSet prev;
    private final List<ExampleRow> exampleRows;

    private AdjustMaxSet(MaxSet prev, List<ExampleRow> exampleRows) {
        this.prev = prev;
        this.exampleRows = exampleRows;
    }

    private MaxSet innerRun() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
