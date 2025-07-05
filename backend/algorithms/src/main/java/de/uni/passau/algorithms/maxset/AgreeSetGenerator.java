package de.uni.passau.algorithms.maxset;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgreeSetGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreeSetGenerator.class);

    public static List<AgreeSet> run(Iterable<StrippedPartition> partitions) {
        final var algorithm = new AgreeSetGenerator();
        return algorithm.innerRun(partitions);
    }

    private List<AgreeSet> innerRun(Iterable<StrippedPartition> partitions) {
        long count = 0;
        long sum = 0;

        for (final StrippedPartition sp : partitions) {
            count++;
            sum += sp.values.size();

            LOGGER.debug("-----");
            LOGGER.debug("Attribute: " + sp.forClass);
            LOGGER.debug("Number of partitions: " + sp.values.size());
        }

        LOGGER.debug("-----");
        LOGGER.debug("Total: " + sum);
        LOGGER.debug("-----");

        final Set<LongList> maxSets = computeMaxSets(partitions);

        Long2ObjectMap<TupleEquivalenceClassRelation> relationships = calculateRelationships(partitions);

        int bitsPerSet = (((int) (count - 1) / 64) + 1) * 64;
        long setsNeeded = 0;
        for (LongList l : maxSets) {
            setsNeeded += l.size() * (l.size() - 1) / 2;
        }

        LOGGER.debug("Approx. RAM needed to store all agree sets: " + bitsPerSet * setsNeeded / 8 / 1024 / 1024 + " MB");

        Set<AgreeSet> agreeSets = computeAgreeSets(relationships, maxSets, partitions);

        return new ArrayList<>(agreeSets);
    }

    private Set<LongList> computeMaxSets(Iterable<StrippedPartition> partitions) {
        LOGGER.debug("\tComputation of maximal partitions");
        long start = System.currentTimeMillis();

        Set<LongList> sortedPartitions = sortPartitions(partitions);

        LOGGER.debug("\tTime to sort: " + (System.currentTimeMillis() - start));

        Iterator<LongList> it = sortedPartitions.iterator();
        long remainingPartitions = sortedPartitions.size();
        LOGGER.debug("\tNumber of Partitions: " + remainingPartitions);

        Long2ObjectMap<LongSet> index = new Long2ObjectOpenHashMap<>();
        Set<LongList> max = new HashSet<>();

        long actuelIndex = 0;
        LongList actuelList;

        while (it.hasNext()) {
            actuelList = it.next();
            handlePartition(actuelList, actuelIndex, index, max);
            actuelIndex++;
        }

        long end = System.currentTimeMillis();
        LOGGER.debug("\tTime needed: " + (end - start));

        index.clear();
        sortedPartitions.clear();

        return max;

    }

    private Set<LongList> sortPartitions(Iterable<StrippedPartition> partitions) {
        Set<LongList> sortedPartitions = new TreeSet<>(new LongListComparator());
        for (StrippedPartition partition : partitions)
            sortedPartitions.addAll(partition.values);

        return sortedPartitions;
    }

    private static class LongListComparator implements Comparator<LongList> {

        @Override public int compare(LongList l1, LongList l2) {
            if (l1.size() - l2.size() != 0)
                return l2.size() - l1.size();

            for (int i = 0; i < l1.size(); i++) {
                if (l1.getLong(i) == l2.getLong(i))
                    continue;

                return (int) (l2.getLong(i) - l1.getLong(i));
            }

            return 0;
        }

    }

    private void handlePartition(LongList actuelList, long position, Long2ObjectMap<LongSet> index, Set<LongList> max) {
        if (!isSubset(actuelList, index)) {
            max.add(actuelList);
            for (long e : actuelList) {
                if (!index.containsKey(e)) {
                    index.put(e, new LongArraySet());
                }
                index.get(e).add(position);
            }
        }
    }

    private Long2ObjectMap<TupleEquivalenceClassRelation> calculateRelationships(Iterable<StrippedPartition> partitions) {
        LOGGER.debug("\tStarted calculation of relationships");

        Long2ObjectMap<TupleEquivalenceClassRelation> relationships = new Long2ObjectOpenHashMap<>();
        for (final StrippedPartition partition : partitions)
            calculateRelationship(partition, relationships);

        return relationships;
    }

    private Set<AgreeSet> computeAgreeSets(Long2ObjectMap<TupleEquivalenceClassRelation> relationships, Set<LongList> maxSets, Iterable<StrippedPartition> partitions) {
        LOGGER.debug("\tStarted calculation of agree sets");

        Set<AgreeSet> agreeSets = new HashSet<>();

        int debugIndex = 0;
        for (LongList maxEquiClass : maxSets) {
            LOGGER.debug("{}", debugIndex++);

            for (int i = 0; i < maxEquiClass.size() - 1; i++) {
                for (int j = i + 1; j < maxEquiClass.size(); j++) {
                    final var iRelation = relationships.get(maxEquiClass.getLong(i));
                    final var jRelation = relationships.get(maxEquiClass.getLong(j));
                    iRelation.intersectWithAndAddToAgreeSet(jRelation, agreeSets);
                }
            }
        }

        return agreeSets;
    }

    private void calculateRelationship(StrippedPartition partition, Long2ObjectMap<TupleEquivalenceClassRelation> relationships) {
        int partitionIndex = 0;
        for (LongList value : partition.values) {
            LOGGER.debug(".");
            for (long index : value) {
                final var relationshipsForIndex = relationships.computeIfAbsent(index, x -> new TupleEquivalenceClassRelation());
                relationshipsForIndex.addRelationship(partition.forClass, partitionIndex);
            }
            partitionIndex++;
        }
    }

    private boolean isSubset(LongList actuelList, Map<Long, LongSet> index) {
        boolean isFirst = true;
        LongSet positions = new LongArraySet();
        for (long e : actuelList) {
            if (!index.containsKey(e))
                return false;

            if (isFirst) {
                isFirst = false;
                positions.addAll(index.get(e));
            } else {
                intersect(positions, index.get(e));
                // FIXME: Throws UnsupportedOperationExeption within fastUtil
                // positions.retainAll(index.get(e));
            }

            if (positions.isEmpty())
                return false;
        }

        return true;
    }

    private void intersect(LongSet positions, LongSet indexSet) {
        final LongSet toRemove = new LongArraySet();
        for (long position : positions) {
            if (!indexSet.contains(position))
                toRemove.add(position);
        }

        positions.removeAll(toRemove);
    }

}
