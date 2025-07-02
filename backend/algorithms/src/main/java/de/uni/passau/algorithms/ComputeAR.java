package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeARException;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.model.MaxSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import java.util.BitSet;
import java.util.ArrayList;


/**
 * Computes Armstrong relation for functional dependencies defined by a maximal set.
 * An original relation is needed to provide values for the generated relation, but it's not used to determine the functional dependencies.
 */
public class ComputeAR {

    public static ArmstrongRelation run(
        /** Represents the actual functional dependencies. */
        //MaxSet maxSet,
        List<MaxSet> maxSets,
        /** The initial relation. */
        Dataset dataset
    ) {
        try {
            //final var algorithm = new ComputeAR(maxSet, dataset);
            final var algorithm = new ComputeAR(maxSets, dataset);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeARException.inner(e);
        }
    }

    //private final MaxSet maxSet;
    private final List<MaxSet> maxSets;
    private final Dataset dataset;

    //private ComputeAR(MaxSet maxSet, Dataset dataset) {
    private ComputeAR(List<MaxSet> maxSets, Dataset dataset) {
        //this.maxSet = maxSet;
        this.maxSets = maxSets;
        this.dataset = dataset;
    }

    private ArmstrongRelation innerRun() {
        //throw new UnsupportedOperationException("Not implemented yet.");

        // Get the number 
        int columnsCount = maxSets.size();
        //System.out.println("DEBUG: Columns count: " + columnsCount);


        // A Set of int[] arrays with a custom comparator
        Set<int[]> sortetArmstrongRelationArraySet = new TreeSet<>((a1, a2) -> {
            int len = Math.min(a1.length, a2.length);
            for (int i = 0; i < len; i++) {
                int cmp = Integer.compare(a1[i], a2[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(a1.length, a2.length);
        });


        // Iterate over each MaxSet 
        for (MaxSet maxSet : maxSets) {
            // Iterate over each functional dependency in the MaxSet
			for (int index = 0; index < maxSet.elements.size(); ++index) {

                // Get the BitSet (LHS) of the current functional dependency
                BitSet bitSet = maxSet.elements.get(index).columns;

                // Convert BitSet to a list of indices
                List<Integer> columnIndexList = new ArrayList();
                int lastIndex = bitSet.nextSetBit(0);
                while (lastIndex != -1) {
                    columnIndexList.add(lastIndex);
                    lastIndex = bitSet.nextSetBit(lastIndex + 1);
                }

               
                // A array with length of classes 
				int[] row = new int[columnsCount];
				columnIndexList.forEach(element -> {
					row[element] = -1;
				});

                // 
				for (int i = 0; i < columnsCount; ++i) {
					if (row[i] == -1) {
						row[i] = 0;
					} else {
						row[i] = 1;
					}
				}
				sortetArmstrongRelationArraySet.add(row);
			}
		}

        // Convert the sorted set to a List
		List<int[]> ARL = new LinkedList<>(sortetArmstrongRelationArraySet);

        // Print the Armstrong relation for debugging
        System.out.println("DEBUG: Armstrong relation:");
        for (int[] row : ARL) {
            StringBuilder sb = new StringBuilder();
            for (int value : row) {
                sb.append(value).append(" ");
            }
            System.out.println(sb.toString().trim());
        }


		// int[] row = new int[columnsCount];
		// ARL.addFirst(row);

		// int lineNumber = 1;
		// for (int[] value : ARL) {

		// 	boolean changed = false;
		// 	for (int index = 0; index < value.length; ++index) {
		// 		if (value[index] == 1) {
		// 			value[index] = lineNumber;
		// 			changed = true;
		// 		}
		// 	}
		// 	if (changed) {
		// 		++lineNumber;
		// 	}
		// }

        // Create the Armstrong relation
        return null;
    }
}
