package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni.passau.algorithms.exception.ComputeFDException;
import de.uni.passau.algorithms.fd.LhsGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.FdSet.Fd;
import de.uni.passau.core.model.ColumnSet;

public class ComputeFds {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeFds.class);

    public static FdSet run(MaxSets maxSets, String[] columns) {
        try {
            final var algorithm = new ComputeFds(maxSets, columns);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeFDException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final String[] columns;

    private ComputeFds(MaxSets maxSets, String[] columns) {
        this.maxSets = maxSets;
        this.columns = columns;
    }

    private FdSet innerRun() {
        final int numberOfColumns = maxSets.sets().size();

        final List<ComplementMaxSet> complementMaxSets = MaxSetGenerator.generateComplementMaxSets(maxSets.sets());
        LOGGER.debug("----- COMPLEMENTS OF MAXIMAL SETS -----");
        LOGGER.debug("size: " + complementMaxSets.size());
        for (int index = 0; index < complementMaxSets.size(); index++) {
            LOGGER.debug("{}", complementMaxSets.get(index));
        }

        final List<List<ColumnSet>> lhss = LhsGenerator.run(complementMaxSets, numberOfColumns);
        LOGGER.debug("----- LEFT HAND SIDES -----");
        LOGGER.debug("size: " + lhss.size());
        for (int index = 0; index < numberOfColumns; index++) {
            List<ColumnSet> columnSets = lhss.get(index);
            LOGGER.debug("For class: {}, size: {}, columSets:\n{}", index, columnSets.size(), columnSets);
        }

        final var groupedFds = groupFdsByLhs(lhss);

        final var fdsList = new ArrayList<Fd>();
        for (final var entry : groupedFds.entrySet()) {
            final var lhs = entry.getKey();
            final var rhs = entry.getValue();
            fdsList.add(new Fd(lhs, rhs));
        }

        return new FdSet(columns, fdsList);
    }

    private static Map<ColumnSet, ColumnSet> groupFdsByLhs(List<List<ColumnSet>> lhss) {
        /** Map of lhs to rhs so that lhs -> rhs is a functional dependency. */
        final var fdsByLhs = new TreeMap<ColumnSet, ColumnSet>();

        for (int classIndex = 0; classIndex < lhss.size(); classIndex++) {
            final List<ColumnSet> lhssforClass = lhss.get(classIndex);

            for (final ColumnSet lhs : lhssforClass) {
                final var rhs = fdsByLhs.get(lhs);
                if (rhs == null) {
                    // If there is no rhs for this lhs, we create a new one.
                    fdsByLhs.put(lhs, ColumnSet.fromIndexes(classIndex));
                }
                else {
                    // If there is already a rhs for this lhs, we add the class index to it.
                    rhs.set(classIndex);
                }
            }
        }

        return fdsByLhs;
    }
}
