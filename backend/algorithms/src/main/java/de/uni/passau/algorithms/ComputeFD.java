package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uni.passau.algorithms.exception.ComputeFDException;
import de.uni.passau.algorithms.fd.LeftHandSideGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.FdSet.Fd;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import de.uni.passau.core.model.ColumnSet;

public class ComputeFD {

    public static FdSet run(MaxSets maxSets, String[] columns) {
        try {
            final var algorithm = new ComputeFD(maxSets, columns);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeFDException.inner(e);
        }
    }

	private final MaxSets maxSets;
    private final String[] columns;

    private ComputeFD(MaxSets maxSets, String[] columns) throws Exception {
		this.maxSets = maxSets;
        this.columns = columns;
    }

    private FdSet innerRun() throws Exception {
		final int numberOfAttributes = maxSets.sets().size();

		List<ComplementMaxSet> cmaxSets = MaxSetGenerator.generateCMAX_SETs(maxSets.sets(), numberOfAttributes);
		System.out.println("----- COMPLEMENTS OF MAXIMAL SETS -----");
		System.out.println("size: " + cmaxSets.size());
		for (int index = 0; index < cmaxSets.size(); ++index) {
			System.out.println(cmaxSets.get(index));
		}
		System.out.println("");
        Int2ObjectMap<List<ColumnSet>> lhss = new LeftHandSideGenerator().execute(cmaxSets, numberOfAttributes);
        System.out.println("----- LEFT HAND SIDES -----");
		System.out.println("size: " + lhss.size());
		for (int index = 0; index < numberOfAttributes; ++index) {
			List<ColumnSet> list = lhss.get(index);
			if (list == null) {
				continue;
			}
			List<IntList> columnList = new ArrayList<>();
			System.out.println("Attribute: " + index + " Size: " + list.size());
			for (int i = 0; i < list.size(); ++i) {
				ColumnSet bitSet = list.get(i);
				IntList columns = new IntArrayList();
				int lastIndex = bitSet.nextSetBit(0);
				while (lastIndex != -1) {
					columns.add(lastIndex);
					lastIndex = bitSet.nextSetBit(lastIndex + 1);
				}
				columnList.add(columns);
				System.out.println(columns);
			}
			// System.out.println(columnList);
		}
        System.out.println("");

		// List<FunctionalDependencyGroup> result = generateFds(lhss);
		// System.out.println("----- FUNCTIONAL DEPENDENCIES -----");
		// System.out.println("size: " + result.size());

		// List<List<FunctionalDependencyGroup>> fdsForClasses = new ArrayList<>();
		// for (int index = 0; index < numberOfAttributes; ++index) {
		// 	List<FunctionalDependencyGroup> list = new ArrayList<>();
		// 	fdsForClasses.add(list);
		// }

		// for (int index = 0; index < result.size(); ++index) {
		// 	FunctionalDependencyGroup fd = result.get(index);
		// 	var list = fdsForClasses.get(fd.getAttributeID());
		// 	list.add(fd);
		// }

        // for (int index = 0; index < fdsForClasses.size(); ++index) {
        //     final var list = fdsForClasses.get(index);
        //     System.out.println("Attribute: " + index + " Size: " + list.size());

        //     for (final var fd : list)
        //         System.out.println(fd);
        // }
		// System.out.println("");

        final var groupedFds = groupFdsByLhs(lhss);

        final var fdsList = new ArrayList<Fd>();
        for (final var entry : groupedFds.entrySet()) {
            final var lhs = entry.getKey();
            final var rhs = entry.getValue();
            fdsList.add(new Fd(lhs, rhs));
        }

		return new FdSet(
            this.columns,
            fdsList
        );
    }

    // private static List<FunctionalDependencyGroup> generateFds(Int2ObjectMap<List<ColumnSet>> lhss) {
	// 	final var result = new ArrayList<FunctionalDependencyGroup>();

	// 	for (int attribute : lhss.keySet()) {
	// 		for (ColumnSet lhs : lhss.get(attribute)) {
	// 			if (lhs.get(attribute)) {
	// 				continue;
	// 			}
	// 			IntList bits = lhs.convertToIntList();

	// 			FunctionalDependencyGroup fdg = new FunctionalDependencyGroup(attribute, bits);
	// 			result.add(fdg);
	// 		}
	// 	}

	// 	return result;
	// }

    private static Map<ColumnSet, ColumnSet> groupFdsByLhs(Int2ObjectMap<List<ColumnSet>> lhss) {
        /** Map of lhs to rhs so that lhs -> rhs is a functional dependency. */
        final var fdsByLhs = new TreeMap<ColumnSet, ColumnSet>();

        for (final int classIndex : lhss.keySet()) {
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
