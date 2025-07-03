package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.algorithms.fd.LeftHandSideGenerator;
import de.uni.passau.algorithms.fd.FunctionalDependencyGroup;
import de.uni.passau.algorithms.fd.FunctionalDependencyGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.MaxSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import de.uni.passau.core.model.ColumnSet;

public class ComputeFD {

    public static List<MaxSet> run(Dataset dataset) {
        try {
			List<MaxSet> maxSets = ComputeMaxSet.run(dataset);
            final var algorithm = new ComputeFD(dataset, maxSets);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

	public static List<MaxSet> run(Dataset dataset, List<MaxSet> maxSets) {
        try {
            final var algorithm = new ComputeFD(dataset, maxSets);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

    private final Dataset dataset;
	private final List<MaxSet> maxSets;

    private ComputeFD(Dataset dataset, List<MaxSet> maxSets) throws Exception {
        this.dataset = dataset;
		this.maxSets = maxSets;
    }

    private List<MaxSet> innerRun() throws Exception {
		int numberOfAttributes = dataset.getMetadata().getNumberOfColumns();

		List<ComplementMaxSet> cmaxSets = MaxSetGenerator.generateCMAX_SETs(maxSets, numberOfAttributes);
		System.out.println("----- COMPLEMENTS OF MAXIMAL SETS -----");
		System.out.println("size: " + cmaxSets.size());
		for (int index = 0; index < cmaxSets.size(); ++index) {
			System.out.println(cmaxSets.get(index));
		}
		System.out.println("");
        Int2ObjectMap<List<ColumnSet>> lhss = new LeftHandSideGenerator().execute(cmaxSets, numberOfAttributes);
        List<String> columnNames = Arrays.asList(dataset.getHeader());
        FunctionalDependencyGenerator xxx = new FunctionalDependencyGenerator(dataset, dataset.getMetadata().getFilename(), columnNames, lhss);
		List<FunctionalDependencyGroup> result = xxx.execute();
		System.out.println("----- FUNCTIONAL DEPENDENCIES -----");
		System.out.println("size: " + result.size());
		Map<Integer, List<FunctionalDependencyGroup>> fds = new TreeMap<>();
		for (int index = 0; index < numberOfAttributes; ++index) {
			List<FunctionalDependencyGroup> list = new ArrayList<>();
			fds.put(index, list);
		}
		return maxSets;
    }

}