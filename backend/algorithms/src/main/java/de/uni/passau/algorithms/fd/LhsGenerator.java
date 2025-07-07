package de.uni.passau.algorithms.fd;

import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.ColumnSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LhsGenerator {

    /**
     * Computes the LHS
     *
     * @param maxSets The set of the complements of maximal sets (see Phase 2 for further information).
     * @return {@code List<List<OpenBitSet>>} - list of sets of all lefthand sides (ordered by their classes).
     */
    public static List<List<ColumnSet>> run(List<ComplementMaxSet> maxSets) {
        final var algorithm = new LhsGenerator();
        return algorithm.innerRun(maxSets);
    }

    private List<List<ColumnSet>> innerRun(List<ComplementMaxSet> maxSets) {
        final List<List<ColumnSet>> lhss = new ArrayList<>();

        // 1: for all attributes A in R do
        for (final var maxSet : maxSets) {
            // 2: i:=1
            // 3: Li:={B | B in X, X in cmax(dep(r),A)} */
            Set<ColumnSet> Li = new HashSet<>();
            generateFirstLevel(maxSet, Li);

            final var lhssForClass = new ArrayList<ColumnSet>();

            // 4: while Li != ø do
            while (!Li.isEmpty()) {
                // 5: LHSi[A]:={l in Li | l intersect X != ø, for all X in cmax(dep(r),A)}
                final List<ColumnSet> lhs_i = findLhs(Li, maxSet);

                // 6: Li:=Li/LHSi[A]
                Li.removeAll(lhs_i);

                //  7: Li+1:={l' | |l'|=i+1 and for all l subset l' | |l|=i, l in Li}
                // The generation of the next level is, as mentioned in the paper, done with the Apriori gen-function from the following paper:
                // "Fast algorithms for mining association rules in large databases." - Rakesh Agrawal, Ramakrishnan Srikant
                Li = generateNextLevel(Li);

                // 8: i:=i+1
                // 9: lhs(dep(r),A):= union LHSi[A]
                lhssForClass.addAll(lhs_i);
            }

            lhss.add(lhssForClass);
        }

        return lhss;
    }

    private List<ColumnSet> findLhs(Set<ColumnSet> Li, ComplementMaxSet complementMaxSet) {
        final List<ColumnSet> lhs_i = new ArrayList<>();
        for (final ColumnSet l : Li) {
            final boolean isLhs = !complementMaxSet.elements.stream().allMatch(element -> l.intersects(element));
            if (isLhs)
                lhs_i.add(l);
        }

        return lhs_i;
    }

    private void generateFirstLevel(ComplementMaxSet maxSet, Set<ColumnSet> Li) {
        for (final ColumnSet columnSet : maxSet.elements) {
            final var indexes = columnSet.toIndexes();
            for (final var index : indexes)
                Li.add(ColumnSet.fromIndexes(index));
        }
    }

    private Set<ColumnSet> generateNextLevel(Set<ColumnSet> li) {
        // Join-Step
        final List<ColumnSet> Ck = new ArrayList<>();
        for (final ColumnSet p : li) {
            for (final ColumnSet q : li) {
                if (!checkJoinCondition(p, q))
                    continue;

                final ColumnSet candidate = p.clone();
                candidate.or(q);
                Ck.add(candidate);
            }
        }

        // Pruning-Step
        final Set<ColumnSet> result = new HashSet<>();
        for (final ColumnSet c : Ck) {
            boolean prune = false;

            for (final int index : c.toIndexes()) {
                c.flip(index);

                if (!li.contains(ColumnSet.fromIndexes(index))) {
                    prune = true;
                    break;
                }

                c.flip(index);
            }

            if (!prune)
                result.add(c);
        }

        return result;
    }

    private boolean checkJoinCondition(ColumnSet p, ColumnSet q) {
        if (p.lastIndex() >= q.lastIndex())
            return false;

        for (int i = 0; i < p.lastIndex(); i++) {
            if (p.get(i) != q.get(i))
                return false;
        }

        return true;
    }

}
