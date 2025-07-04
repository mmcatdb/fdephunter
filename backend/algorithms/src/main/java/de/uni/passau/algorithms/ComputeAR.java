package de.uni.passau.algorithms;

import de.uni.passau.algorithms.exception.ComputeARException;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.example.ArmstrongRelation;
import de.uni.passau.core.example.ExampleRow;
import de.uni.passau.core.model.MaxSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.stream.Collectors;


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



    private class RowObject implements Comparable<RowObject> {

		public String value;
		public int index;

		private RowObject(String content, int size) {
			this.value = content;
			this.index = size;
		}

		@Override
		public int compareTo(RowObject o) {
			return value.compareTo(o.value);
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

    private ArmstrongRelation innerRun() throws Exception {

        // Get the number
        int columnsCount = maxSets.size();

        // A Set of int[] arrays with a custom comparator
		Set<int[]> sortetArmstrongRelationArraySet = new TreeSet<int[]>((a1, a2) -> {
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
            // Iterate over the list of LHS columns of the functional dependecies in this MaxSet (class)
			maxSet.getAllColumnSets().forEach(lhsBitSet -> {

                // Get the BitSet (LHS) of the current functional dependency. The bitset represents the columns of the LHS.

                // Convert the BitSet to a list of indices of the columns
				List<Integer> lhsColumnIndeces = new ArrayList<Integer>();
                int lastIndex = lhsBitSet.nextSetBit(0);
                while (lastIndex != -1) {
                    lhsColumnIndeces.add(lastIndex);
                    lastIndex = lhsBitSet.nextSetBit(lastIndex + 1);
                }
                // Initialize a array with length of all classes of maxSet
				int[] row = new int[columnsCount];
                // Fill the array with 1
                for (int i = 0; i < columnsCount; ++i) {
                    row[i] = 1;
                }
                // Set rows to 0 for the columns of the LHS
                lhsColumnIndeces.forEach(element -> row[element] = 0);
                // Add the row to the sorted set
				sortetArmstrongRelationArraySet.add(row);
			});
		}

        // Convert the sorted set to a List
		LinkedList<int[]> armstrongRelationList = new LinkedList<>(sortetArmstrongRelationArraySet);

        // Add the first row with all 0s
		int[] row = new int[columnsCount];
        armstrongRelationList.addFirst(row);

		int lineNumber = 1;
        // Iterate over the LHS rows and replace the 1s with line numbers
		for (int[] lhsRow : armstrongRelationList) {
			boolean changed = false;
			for (int index = 0; index < lhsRow.length; ++index) {
				if (lhsRow[index] == 1) {
					lhsRow[index] = lineNumber;
					changed = true;
				}
			}
			if (changed) {
				++lineNumber;
			}
		}

        List<List<String>> mappedUniqueLists = realworldAR(armstrongRelationList);

        // DEBUG: Print the mapped unique lists
        System.out.println("Mapped Unique Lists:");
        mappedUniqueLists.forEach(list -> {
            System.out.println(list.stream().collect(Collectors.joining(", ")));
        });

        // TODO: Create ArmstrongRelation object

        final var referenceRow = mappedUniqueLists.get(0).toArray(String[]::new);

        final var exampleRows = new ArrayList<ExampleRow>();
        for (int i = 1; i < referenceRow.length; i++) {
            final var values = mappedUniqueLists.get(i).toArray(String[]::new);
            final var exampleRow = new ExampleRow(
                values,
                null, // TODO
                null, // TODO
                false // TODO: isEvaluatingPositives
            );
            exampleRows.add(exampleRow);
        }

        return new ArmstrongRelation(
            dataset.getHeader(),
            referenceRow,
            exampleRows,
            true // TODO: isEvaluatingPositives
        );
    }

	private List<List<String>> realworldAR(LinkedList<int[]> AR) throws Exception {

        // The number of columns in the Armstrong Relation
		int columnCount = AR.get(0).length;

		// Step 1: Calculate distinct values required for each column
		int[] distinct = calculateDistinctValues(AR, columnCount);

		// Step 2: Collect unique values for each column from CSV
		List<Set<RowObject>> uniqueValues = collectUniqueValues(columnCount, distinct);
		List<List<String>> uniqueOrderedLists = orderUniqueValues(uniqueValues);

		// Step 3: Ensure each uniqueValues list has the required distinct values
		List<List<String>> uniqueLists = ensureDistinctValues(uniqueOrderedLists, distinct);

		// Step 4: Map AR indices to their corresponding values from uniqueLists
		return mapIndicesToValues(AR, uniqueLists, columnCount);
	}

	private int[] calculateDistinctValues(List<int[]> AR, int columnCount) {

        // Initialize list of {columnCount} sets.
		List<Set<Integer>> uniqueValuesSet = new ArrayList<>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			uniqueValuesSet.add(new TreeSet<>());
		}

        // Iterate over each LHS column in the Armstrong Relation
		for (int[] lhsRow : AR) {
            // Add the columns values of the LHS to the sets in order
			for (int col = 0; col < columnCount; col++) {
				uniqueValuesSet.get(col).add(lhsRow[col]);
			}
		}

        // Add the size of each set to the distinct array
		int[] distinct = new int[columnCount];
		for (int i = 0; i < columnCount; i++) {
			distinct[i] = uniqueValuesSet.get(i).size();
		}

		return distinct;
	}

	private List<Set<RowObject>> collectUniqueValues(int columnCount, int[] distinct) throws Exception {

        // Initialize list of {columnCount} sets of RowObject. This can hold the value and the index of the value.
		List<Set<RowObject>> uniqueValues = new ArrayList<>();
		for (int i = 0; i < columnCount; i++) {
			uniqueValues.add(new TreeSet<>());
		}

        // Iterate over each line in the dataset
        for(String[] line: this.dataset.getRows()) {

            boolean allRequirementsMet = true;

            // Iterate over each column in the current line
            for (int lineIdx = 0; lineIdx < line.length; lineIdx++) {
                // Get the content of the current column
				String rowValue = line[lineIdx];
                // If the content is null, set it to "NULL"
				if (rowValue == null) {
					rowValue = "NULL";
				}

                // Get the set of RowObject for the current column index
				Set<RowObject> rowObjectsSet = uniqueValues.get(lineIdx);

                // If the set size is less than the required distinct values for this column
				if (rowObjectsSet.size() < distinct[lineIdx]) {
                    // Add a new RowObject with the row value and its index in the set
					rowObjectsSet.add(new RowObject(rowValue, rowObjectsSet.size()));
				}
                // Check if the set size meets the distinct requirement
				if (rowObjectsSet.size() < distinct[lineIdx]) {
					allRequirementsMet = false;
				}
			}
			if (allRequirementsMet) {
				break;
			}

        }
		return uniqueValues;
	}

    private List<List<String>> orderUniqueValues(List<Set<RowObject>> uniqueValues) {
        // Convert each set of RowObject into a List<String> where the index corresponds to the original index
		List<List<String>> list = new ArrayList<>();
		for (Set<RowObject> set : uniqueValues) {
			int size = set.size();
			String[] array = new String[size];
			for (RowObject tuple : set) {
				array[tuple.index] = tuple.value;
			}
			List<String> l = List.of(array);
			list.add(l);
		}
		return list;
	}


	private List<List<String>> ensureDistinctValues(List<List<String>> uniqueValues, int[] distinct) {
		List<List<String>> uniqueLists = new ArrayList<>();
		for (int i = 0; i < uniqueValues.size(); i++) {
			List<String> columnList = new ArrayList<>(uniqueValues.get(i));
			while (columnList.size() < distinct[i]) {
				columnList.add("Dummy#" + columnList.size());
			}
			uniqueLists.add(columnList);
		}
		return uniqueLists;
	}

	private List<List<String>> mapIndicesToValues(List<int[]> AR, List<List<String>> uniqueLists, int columnCount) {
		List<List<String>> result = new ArrayList<>();
		int[] lastValue = new int[columnCount];
		int[] currentIndex = new int[columnCount];

		for (int[] row : AR) {
			List<String> newRow = new ArrayList<>();
			for (int colIdx = 0; colIdx < columnCount; colIdx++) {
				int value = row[colIdx];
				if (value == 0) {
					newRow.add(uniqueLists.get(colIdx).get(0));
					continue;
				}
				if (value > lastValue[colIdx]) {
					currentIndex[colIdx]++;
					lastValue[colIdx] = value;
				}
				newRow.add(uniqueLists.get(colIdx).get(currentIndex[colIdx]));
			}
			result.add(newRow);
		}
		return result;
	}

}
