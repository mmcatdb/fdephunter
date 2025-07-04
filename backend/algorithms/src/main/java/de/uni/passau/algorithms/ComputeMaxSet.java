package de.uni.passau.algorithms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.algorithms.maxset.AgreeSetGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.algorithms.maxset.StrippedPartitionGenerator;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.AgreeSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.StrippedPartition;

public class ComputeMaxSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeMaxSet.class);

    public static MaxSets run(Dataset dataset) {
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

    private MaxSets innerRun() throws Exception {
        StrippedPartitionGenerator spg = new StrippedPartitionGenerator();
		List<StrippedPartition> strippedPartitions = spg.execute(dataset);

		LOGGER.debug("----- STRIPPED PARTITIONS -----");
		LOGGER.debug("size: " + strippedPartitions.size());
		for (int index = 0; index < strippedPartitions.size(); ++index) {
			LOGGER.debug("{}", strippedPartitions.get(index));
		}
		LOGGER.debug("");

		int length = dataset.getMetadata().getNumberOfColumns();

		List<AgreeSet> agreeSets = new AgreeSetGenerator().execute(strippedPartitions);
		LOGGER.debug("----- AGREE SET -----");
		LOGGER.debug("size: " + agreeSets.size());
		for (int index = 0; index < agreeSets.size(); ++index) {
			LOGGER.debug("{}", agreeSets.get(index));
		}
		LOGGER.debug("");

		MaxSetGenerator setGenerator = new MaxSetGenerator(agreeSets, length);
		List<MaxSet> maxSets = setGenerator.generateMaxSet();
		LOGGER.debug("----- MAXIMAL SETS -----");
		LOGGER.debug("size: " + maxSets.size());
		for (int index = 0; index < maxSets.size(); ++index) {
			LOGGER.debug("{}", maxSets.get(index));
		}

        return new MaxSets(maxSets);
    }

}
//
