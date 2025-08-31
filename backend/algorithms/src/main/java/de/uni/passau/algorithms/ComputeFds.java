package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uni.passau.algorithms.exception.ComputeFDException;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.FdSet.Fd;
import de.uni.passau.core.model.ColumnSet;

public class ComputeFds {

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
    private final int numberOfColumns;

    private ComputeFds(MaxSets maxSets, String[] columns) {
        this.maxSets = maxSets;
        this.columns = columns;
        this.numberOfColumns = maxSets.sets().size();
    }

    private FdSet innerRun() {
        final List<Set<ColumnSet>> lhss = new ArrayList<>();
        for (final MaxSet maxSet : maxSets.sets())
            lhss.add(computeLhssForMaxSet(maxSet));

        final var groupedFds = groupFdsByLhs(lhss);

        final var fdsList = new ArrayList<Fd>();
        for (final var entry : groupedFds.entrySet()) {
            final var lhs = entry.getKey();
            final var rhs = entry.getValue();
            fdsList.add(new Fd(lhs, rhs));
        }

        return new FdSet(columns, fdsList);
    }

    private Set<ColumnSet> computeLhssForMaxSet(MaxSet set) {
        final Set<ColumnSet> output = computeLhssForSizeOne(set);

        // For each max set element, we add all one-larger supersets to the output.
        // Then we will need to prune the output.

        set.elements()
            // Sort the elements so that the smallest ones are processed first. We do this because only a smaller set can be a subset of a larger one.
            .sorted((a, b) -> a.size() - b.size())
            .forEach(element -> {
                for (final var superset : element.toSupersets(numberOfColumns)) {
                    // We don't want this one.
                    if (superset.get(set.forClass))
                        continue;

                    // The pruning is done here. If there is already a smaller LHS, we don't need to add this one.
                    boolean isSuperset = false;
                    for (final var outputElement : output) {
                        if (superset.isSupersetOf(outputElement)) {
                            isSuperset = true;
                            break;
                        }
                    }

                    if (!isSuperset)
                        output.add(superset);
                }
            });

        return output;

    }

    private Set<ColumnSet> computeLhssForSizeOne(MaxSet maxSet) {
        Set<ColumnSet> output = new HashSet<>();

        // Each not-used index will represent a single column in the LHS
        final var usedIndexes = ColumnSet.empty();

        maxSet.elements().forEach(element -> usedIndexes.or(element));

        final var unusedIndexes = usedIndexes.toInverse(numberOfColumns);
        // This one would be turned on by the previous line, but we don't want it.
        unusedIndexes.clear(maxSet.forClass);

        for (final int index : unusedIndexes.toIndexes())
            output.add(ColumnSet.fromIndex(index));

        return output;
    }

    private static Map<ColumnSet, ColumnSet> groupFdsByLhs(List<Set<ColumnSet>> lhss) {
        /** Map of lhs to rhs so that lhs -> rhs is a functional dependency. */
        final var fdsByLhs = new TreeMap<ColumnSet, ColumnSet>();

        for (int classIndex = 0; classIndex < lhss.size(); classIndex++) {
            final Set<ColumnSet> lhssforClass = lhss.get(classIndex);

            for (final ColumnSet lhs : lhssforClass) {
                final var rhs = fdsByLhs.get(lhs);
                if (rhs == null) {
                    // If there is no rhs for this lhs, we create a new one.
                    fdsByLhs.put(lhs, ColumnSet.fromIndex(classIndex));
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
