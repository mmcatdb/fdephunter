package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uni.passau.algorithms.exception.ComputeFDException;
import de.uni.passau.algorithms.fd.LeftHandSideGenerator;
import de.uni.passau.algorithms.fd.FunctionalDependencyGroup;
import de.uni.passau.algorithms.fd.FunctionalDependencyGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.MaxSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import de.uni.passau.core.model.ColumnSet;

public class ComputeFD {

    public static Map<Integer, List<FunctionalDependencyGroup>> run(Dataset dataset) {
        try {
			List<MaxSet> maxSets = ComputeMaxSet.run(dataset);
            final var algorithm = new ComputeFD(dataset, maxSets);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeFDException.inner(e);
        }
    }

	public static Map<Integer, List<FunctionalDependencyGroup>> run(Dataset dataset, List<MaxSet> maxSets) {
        try {
            final var algorithm = new ComputeFD(dataset, maxSets);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeFDException.inner(e);
        }
    }

    private final Dataset dataset;
	private final List<MaxSet> maxSets;

    private ComputeFD(Dataset dataset, List<MaxSet> maxSets) throws Exception {
        this.dataset = dataset;
		this.maxSets = maxSets;
    }

    private Map<Integer, List<FunctionalDependencyGroup>> innerRun() throws Exception {
		int numberOfAttributes = dataset.getMetadata().getNumberOfColumns();

		List<ComplementMaxSet> cmaxSets = MaxSetGenerator.generateCMAX_SETs(maxSets, numberOfAttributes);
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

        // get column names if available, otherwise use indices as names
        List<String> columnNames = dataset.getMetadata().hasHeader() 
            ? Arrays.asList(dataset.getHeader())
            : java.util.stream.IntStream.range(0, numberOfAttributes)
                .mapToObj(String::valueOf)
                .collect(java.util.stream.Collectors.toList());

        FunctionalDependencyGenerator xxx = new FunctionalDependencyGenerator(dataset, dataset.getMetadata().getFilename(), columnNames, lhss);
		List<FunctionalDependencyGroup> result = xxx.execute();
		System.out.println("----- FUNCTIONAL DEPENDENCIES -----");
		System.out.println("size: " + result.size());
		Map<Integer, List<FunctionalDependencyGroup>> fds = new TreeMap<>();
		for (int index = 0; index < numberOfAttributes; ++index) {
			List<FunctionalDependencyGroup> list = new ArrayList<>();
			fds.put(index, list);
		}
		for (int index = 0; index < result.size(); ++index) {
			FunctionalDependencyGroup fd = result.get(index);
			var list = fds.get(fd.getAttributeID());
			list.add(fd);
		}

		fds.forEach((key, list) -> {
			System.out.println("Attribute: " + key + " Size: " + list.size());
			for (int index = 0; index < list.size(); ++index) {
				System.out.println(list.get(index));
			}
		});
		System.out.println("");
		return fds;
    }
}