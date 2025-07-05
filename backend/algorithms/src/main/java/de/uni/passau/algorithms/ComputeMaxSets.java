package de.uni.passau.algorithms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni.passau.algorithms.exception.ComputeMaxSetException;
import de.uni.passau.algorithms.maxset.AgreeSet;
import de.uni.passau.algorithms.maxset.AgreeSetGenerator;
import de.uni.passau.algorithms.maxset.MaxSetGenerator;
import de.uni.passau.algorithms.maxset.StrippedPartition;
import de.uni.passau.algorithms.maxset.StrippedPartitionGenerator;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

public class ComputeMaxSets {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeMaxSets.class);

    public static MaxSets run(Dataset dataset) {
        try {
            final var algorithm = new ComputeMaxSets(dataset);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ComputeMaxSetException.inner(e);
        }
    }

    private final Dataset dataset;

    private ComputeMaxSets(Dataset dataset) {
        this.dataset = dataset;
    }

    private MaxSets innerRun() {
        final List<StrippedPartition> strippedPartitions = StrippedPartitionGenerator.run(dataset);

        LOGGER.debug("----- STRIPPED PARTITIONS -----");
        LOGGER.debug("size: " + strippedPartitions.size());
        for (int index = 0; index < strippedPartitions.size(); index++) {
            LOGGER.debug("{}", strippedPartitions.get(index));
        }
        LOGGER.debug("");

        int numberOfColumns = dataset.getMetadata().numberOfColumns();

        List<AgreeSet> agreeSets = AgreeSetGenerator.run(strippedPartitions);
        LOGGER.debug("----- AGREE SET -----");
        LOGGER.debug("size: " + agreeSets.size());
        for (int index = 0; index < agreeSets.size(); index++) {
            LOGGER.debug("{}", agreeSets.get(index));
        }
        LOGGER.debug("");

        List<MaxSet> maxSets = MaxSetGenerator.generateMaxSets(agreeSets, numberOfColumns);
        LOGGER.debug("----- MAXIMAL SETS -----");
        LOGGER.debug("size: " + maxSets.size());
        for (int index = 0; index < maxSets.size(); index++) {
            LOGGER.debug("{}", maxSets.get(index));
        }

        return new MaxSets(maxSets);
    }

}
//
