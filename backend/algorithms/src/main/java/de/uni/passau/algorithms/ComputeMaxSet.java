package de.uni.passau.algorithms;

import java.util.List;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.algorithms.maxset.AgreeSetGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.algorithms.maxset.StrippedPartitionGenerator;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.AgreeSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.StrippedPartition;

public class ComputeMaxSet {

    public static List<MaxSet> run(Dataset dataset) {
        try {
            final var algorithm = new ComputeMaxSet(dataset);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

    private final Dataset dataset;

    private ComputeMaxSet(Dataset dataset) throws Exception {
        this.dataset = dataset;
    }

    private List<MaxSet> innerRun() throws Exception {
        StrippedPartitionGenerator spg = new StrippedPartitionGenerator();
		List<StrippedPartition> strippedPartitions = spg.execute(dataset);

		System.out.println("----- STRIPPED PARTITIONS -----");
		System.out.println("size: " + strippedPartitions.size());
		for (int index = 0; index < strippedPartitions.size(); ++index) {
			System.out.println(strippedPartitions.get(index));
		}
		System.out.println("");

		int length = dataset.getMetadata().getNumberOfColumns();

		List<AgreeSet> agreeSets = new AgreeSetGenerator().execute(strippedPartitions);
		System.out.println("----- AGREE SET -----");
		System.out.println("size: " + agreeSets.size());
		for (int index = 0; index < agreeSets.size(); ++index) {
			System.out.println(agreeSets.get(index));
		}
		System.out.println("");

		MaxSetGenerator setGenerator = new MaxSetGenerator(agreeSets, length);
		List<MaxSet> maxSets = setGenerator.generateMaxSet();
		System.out.println("----- MAXIMAL SETS -----");
		System.out.println("size: " + maxSets.size());
		for (int index = 0; index < maxSets.size(); ++index) {
			System.out.println(maxSets.get(index));
		}

        return maxSets;
    }

}
//