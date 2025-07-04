package de.uni.passau.algorithms.maxset;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni.passau.core.model.AgreeSet;
import de.uni.passau.core.model.StrippedPartition;
import de.uni.passau.core.model.TupleEquivalenceClassRelation;

/**
 *
 * @author pavel.koupil
 */
public class AgreeSetGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreeSetGenerator.class);

	private static class ListComparator2 implements Comparator<LongList> {

		@Override
		public int compare(LongList l1, LongList l2) {

			if (l1.size() - l2.size() != 0) {
				return l2.size() - l1.size();
			}
			for (int i = 0; i < l1.size(); i++) {
				if (l1.getLong(i) == l2.getLong(i)) {
					continue;
				}
				return (int) (l2.getLong(i) - l1.getLong(i));
			}
			return 0;
		}

	}

	public AgreeSetGenerator() {}

	public List<AgreeSet> execute(List<StrippedPartition> partitions) throws Exception {

        long sum = 0;
        for (StrippedPartition p : partitions) {
            LOGGER.debug("-----");
            LOGGER.debug("Attribute: " + p.getAttributeID());
            LOGGER.debug("Number of partitions: " + p.getValues().size());
            sum += p.getValues().size();
        }
        LOGGER.debug("-----");
        LOGGER.debug("Total: " + sum);
        LOGGER.debug("-----");

		Set<LongList> maxSets;
//        if (this.chooseAlternative1) {
//            maxSets = this.computeMaximumSetsAlternative(partitions);
//        } else if (this.chooseAlternative2) {
		maxSets = this.computeMaximumSetsAlternative2(partitions);
//        } else {
//            maxSets = this.computeMaximumSets(partitions);
//        }

		Long2ObjectMap<TupleEquivalenceClassRelation> relationships = calculateRelationships(partitions);
		Set<AgreeSet> agreeSets = computeAgreeSets(relationships, maxSets, partitions);

		List<AgreeSet> result = new LinkedList<>(agreeSets);

		return result;
	}

	public Set<LongList> computeMaximumSetsAlternative2(List<StrippedPartition> partitions) throws Exception {

        LOGGER.debug("\tComputation of maximal partitions");
		long start = System.currentTimeMillis();

		Set<LongList> sortedPartitions = this.sortPartitions(partitions, new ListComparator2());

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
			this.handlePartition(actuelList, actuelIndex, index, max);
			actuelIndex++;
		}

		long end = System.currentTimeMillis();
        LOGGER.debug("\tTime needed: " + (end - start));

		index.clear();
		sortedPartitions.clear();

		return max;

	}

	private Set<LongList> sortPartitions(List<StrippedPartition> partitions, Comparator<LongList> comparator) {

		Set<LongList> sortedPartitions = new TreeSet<>(comparator);
		for (StrippedPartition p : partitions) {
			sortedPartitions.addAll(p.getValues());
		}
		return sortedPartitions;
	}

	private void handlePartition(LongList actuelList, long position, Long2ObjectMap<LongSet> index, Set<LongList> max) {

		if (!this.isSubset(actuelList, index)) {
			max.add(actuelList);
			for (long e : actuelList) {
				if (!index.containsKey(e)) {
					index.put(e, new LongArraySet());
				}
				index.get(e).add(position);
			}
		}
	}

	public Long2ObjectMap<TupleEquivalenceClassRelation> calculateRelationships(List<StrippedPartition> partitions) {

        LOGGER.debug("\tStarted calculation of relationships");
		Long2ObjectMap<TupleEquivalenceClassRelation> relationships = new Long2ObjectOpenHashMap<>();
		for (StrippedPartition p : partitions) {
			this.calculateRelationship(p, relationships);
		}

		return relationships;
	}

	public Set<AgreeSet> computeAgreeSets(Long2ObjectMap<TupleEquivalenceClassRelation> relationships, Set<LongList> maxSets, List<StrippedPartition> partitions) throws Exception {

        LOGGER.debug("\tStarted calculation of agree sets");
        int bitsPerSet = (((int) (partitions.size() - 1) / 64) + 1) * 64;
        long setsNeeded = 0;
        for (LongList l : maxSets) {
            setsNeeded += l.size() * (l.size() - 1) / 2;
        }
        LOGGER.debug("Approx. RAM needed to store all agree sets: " + bitsPerSet * setsNeeded / 8 / 1024 / 1024 + " MB");

		partitions.clear();

        LOGGER.debug("{}", maxSets.size());
		int a = 0;

		Set<AgreeSet> agreeSets = new HashSet<>();

		for (LongList maxEquiClass : maxSets) {
            LOGGER.debug("{}", a++);
			for (int i = 0; i < maxEquiClass.size() - 1; i++) {
				for (int j = i + 1; j < maxEquiClass.size(); j++) {
					relationships.get(maxEquiClass.getLong(i)).intersectWithAndAddToAgreeSet(relationships.get(maxEquiClass.getLong(j)), agreeSets);
				}
			}
		}

		return agreeSets;

	}

	private void calculateRelationship(StrippedPartition partitions, Long2ObjectMap<TupleEquivalenceClassRelation> relationships) {

		int partitionNr = 0;
		for (LongList partition : partitions.getValues()) {
            LOGGER.debug(".");
			for (long index : partition) {
				if (!relationships.containsKey(index)) {
					relationships.put(index, new TupleEquivalenceClassRelation());
				}
				relationships.get(index).addNewRelationship(partitions.getAttributeID(), partitionNr);
			}
			partitionNr++;
		}

	}

	private boolean isSubset(LongList actuelList, Map<Long, LongSet> index) {

		boolean first = true;
		LongSet positions = new LongArraySet();
		for (long e : actuelList) {
			if (!index.containsKey(e)) {
				return false;
			}
			if (first) {
				positions.addAll(index.get(e));
				first = false;
			} else {

				this.intersect(positions, index.get(e));
				// FIXME: Throws UnsupportedOperationExeption within fastUtil
				// positions.retainAll(index.get(e));
			}
			if (positions.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void intersect(LongSet positions, LongSet indexSet) {

		LongSet toRemove = new LongArraySet();
		for (long l : positions) {
			if (!indexSet.contains(l)) {
				toRemove.add(l);
			}
		}
		positions.removeAll(toRemove);
	}

}
