package de.uni.passau.algorithms.fd;

import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.ColumnSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public class LeftHandSideGenerator {

	public LeftHandSideGenerator() {
	}

	/**
	 * Computes the LHS
	 *
	 * @param maximalSets The set of the complements of maximal sets (see Phase 2 for further information)
	 * @param nrOfAttributes The number attributes in the whole relation
	 * @return {@code Int2ObjectMap<List<OpenBitSet>>} (key: dependent attribute, value: set of all lefthand sides)
	 */
	public Int2ObjectMap<List<ColumnSet>> execute(List<ComplementMaxSet> maximalSets, int nrOfAttributes) {

		Int2ObjectMap<List<ColumnSet>> lhs = new Int2ObjectOpenHashMap<>();

		/* 1: for all attributes A in R do */
		for (int attribute = 0; attribute < nrOfAttributes; attribute++) {
//			System.out.println("Attribute: " + attribute);
			/* 2: i:=1 */
			// int i = 1;

			/* 3: Li:={B | B in X, X in cmax(dep(r),A)} */
			Set<ColumnSet> Li = new HashSet<>();
			ComplementMaxSet correctSet = this.generateFirstLevelAndFindCorrectSet(maximalSets, attribute, Li);
//			System.out.println("Attribute: " + attribute + " after generate first level");

			List<List<ColumnSet>> lhs_a = new LinkedList<>();

			/* 4: while Li != ø do */
//			int counter = 0;
			while (!Li.isEmpty()) {
//				++counter;
//				System.out.println("not empty " + counter + " :: " + Li.size());
				/*
                 * 5: LHSi[A]:={l in Li | l intersect X != ø, for all X in cmax(dep(r),A)}
				 */
				List<ColumnSet> lhs_i = findLHS(Li, correctSet);

				/* 6: Li:=Li/LHSi[A] */
				Li.removeAll(lhs_i);

				/*
                 * 7: Li+1:={l' | |l'|=i+1 and for all l subset l' | |l|=i, l in Li}
				 */
 /*
				 * The generation of the next level is, as mentioned in the paper, done with the Apriori gen-function from the
				 * following paper: "Fast algorithms for mining association rules in large databases." - Rakesh Agrawal,
				 * Ramakrishnan Srikant
				 */
				Li = this.generateNextLevel(Li);

				/* 8: i:=i+1 */
				// i++;
				lhs_a.add(lhs_i);
			}

			/* 9: lhs(dep(r),A):= union LHSi[A] */
			if (!lhs.containsKey(attribute)) {
				lhs.put(attribute, new LinkedList<ColumnSet>());
//				System.out.println("LHS_SIZE: " + lhs.size());
			}
			for (List<ColumnSet> lhs_ia : lhs_a) {
				lhs.get(attribute).addAll(lhs_ia);
			}
		}

//		System.out.println("RETURNING LHS_SIZE: " + lhs.size());
		return lhs;
	}

	private List<ColumnSet> findLHS(Set<ColumnSet> Li, ComplementMaxSet correctSet) {

		List<ColumnSet> lhs_i = new LinkedList<>();
		for (ColumnSet l : Li) {
			boolean isLHS = true;
			for (ColumnSet x : correctSet.combinations()) {
				if (!l.intersects(x)) {
					isLHS = false;
					break;
				}
			}
			if (isLHS) {
				lhs_i.add(l);
			}
		}
		return lhs_i;
	}

	private ComplementMaxSet generateFirstLevelAndFindCorrectSet(List<ComplementMaxSet> maximalSets, int attribute, Set<ColumnSet> Li) {

		ComplementMaxSet correctSet = null;
		for (ComplementMaxSet set : maximalSets) {
			if (!(set.forClass == attribute)) {
				continue;
			}
			correctSet = set;
			for (ColumnSet list : correctSet.combinations()) {

				ColumnSet combination;
				int lastIndex = list.nextSetBit(0);
				while (lastIndex != -1) {
					combination = new ColumnSet();
					combination.set(lastIndex);
					Li.add(combination);
					lastIndex = list.nextSetBit(lastIndex + 1);
				}
			}
			break;
		}
		return correctSet;
	}

	private Set<ColumnSet> generateNextLevel(Set<ColumnSet> li) {

		// Join-Step
		List<ColumnSet> Ck = new LinkedList<>();
		for (ColumnSet p : li) {
			for (ColumnSet q : li) {
				if (!this.checkJoinCondition(p, q)) {
					continue;
				}
				ColumnSet candidate = new ColumnSet();
				candidate.or(p);
				candidate.or(q);
				Ck.add(candidate);
			}
		}

		// Pruning-Step
		Set<ColumnSet> result = new HashSet<>();
		for (ColumnSet c : Ck) {
			boolean prune = false;
			int lastIndex = c.nextSetBit(0);
			while (lastIndex != -1) {
				c.flip(lastIndex);
				if (!li.contains(c)) {
					prune = true;
					break;
				}
				c.flip(lastIndex);
				lastIndex = c.nextSetBit(lastIndex + 1);
			}

			if (!prune) {
				result.add(c);
			}
		}

		return result;

	}

	private boolean checkJoinCondition(ColumnSet p, ColumnSet q) {

		if (p.prevSetBit(p.length()) >= q.prevSetBit(q.length())) {
			return false;
		}
		for (int i = 0; i < p.prevSetBit(p.length()); i++) {
			if (p.get(i) != q.get(i)) {
				return false;
			}
		}
		return true;
	}

}
