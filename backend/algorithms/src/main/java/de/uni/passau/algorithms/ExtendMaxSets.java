package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

public class ExtendMaxSets {

    public static MaxSets run(
        MaxSets maxSets,
        /** The size of the candidates that will be created. */
        int lhsSize
    ) {
        try {
            final var algorithm = new ExtendMaxSets(maxSets, lhsSize);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ExtendMaxSetException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final int lhsSize;

    private final int numberOfColumns;

    private ExtendMaxSets(MaxSets maxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.lhsSize = lhsSize;

        this.numberOfColumns = maxSets.sets().size();
    }

    private MaxSets innerRun() {
        final MaxSets extendedMaxSets = new MaxSets(new ArrayList<>());

        for (final MaxSet original : maxSets.sets()) {
            // Clone the original max set because we don't want to modify it.
            final MaxSet extending = original.clone();

            // If the max set is already finished, we don't need to extend it.
            if (!extending.isFinished()) {
                if (lhsSize == 1)
                    extendMaxSetForSizeOne(extending);
                else
                    extendMaxSet(extending);
            }

            isExtendedMaxSetFinished(extending);
            extending.setFinished();

            extendedMaxSets.sets().add(extending);
        }

        return extendedMaxSets;
    }

    private void extendMaxSetForSizeOne(MaxSet extending) {
        // This is a special case because the one-element elements that we want to generate don't have any subsets.
        for (int i = 0; i < numberOfColumns; i++) {
            if (i == extending.forClass)
                continue; // Skip the class column

            // Add the column as a candidate to the extended max set (only if it's not a confirmed element already).
            final ColumnSet columnSet = ColumnSet.fromIndexes(i);
            if (!extending.hasConfirmed(columnSet))
                extending.addCandidate(columnSet);
        }

        extending.pruneSubsets();
    }

    private void extendMaxSet(MaxSet extending) {
        // Get all MaxSets for the class with size lhsSize - 1.
        final var subsets = extending.elements().filter(element -> element.size() == lhsSize - 1).toList();

        final Map<ColumnSet, Integer> extensionMap = createExtensionMap(extending.forClass, subsets);

        // Now we have a map of all ColumnSets with size lhsSize, and the number of columns from which they can be generated.
        // If the count is equal to lhsSize, it means that they are candidates for the Max Set.
        for (final Entry<ColumnSet, Integer> entry : extensionMap.entrySet()) {
            final int count = entry.getValue();
            if (count == lhsSize) {
                entry.getKey();
                // Add the ColumnSet as a candidate to the extended max set
                extending.addCandidate(entry.getKey());
            }
        }

        extending.pruneSubsets();
    }

    /**
     * Count for each ColumnSet wiht size lhsSize, from how many columns it can be generated
     * by adding one column to a column in extended max set (i.e., to elements of the max Set with size lhsSize - 1).
     * All ColumnSets for which this count is equal to lhsSize are candidates for the max set.
     * The key is the ColumnSet, the value is the count.
     */
    private Map<ColumnSet, Integer> createExtensionMap(int forClass, List<ColumnSet> subsets) {
        final Map<ColumnSet, Integer> output = new HashMap<>();

        for (int i = 0; i < numberOfColumns; i++) {
            // Skip the class column
            if (i == forClass)
                continue;

            // Add all combinations of size lhsSize - 1 to the confirmed max set.
            for (final ColumnSet columnSet : subsets) {
                if (columnSet.get(i)) {
                    // Skip the column if it's already in the column set.
                    continue;
                }

                final ColumnSet newColumnSet = columnSet.clone();
                newColumnSet.set(i); // Add the column to the column set.

                final var current = output.get(newColumnSet);
                output.put(newColumnSet, current == null ? 1 : current + 1);
            }
        }

        return output;
    }

    private boolean isExtendedMaxSetFinished(MaxSet extended) {
        // If there are no elements with size >= lhsSize, the max set is finished.
        // First, we check the candidates (they have to have size = lhsSize). This is just an optimization.
        if (extended.candidateCount() > 0)
            return false;

        // Now we check the rest.
        for (final var element : extended.confirmedElements())
            if (element.size() >= lhsSize)
                return false;

        return true;

        // Another option would be to check whether the (lhsSize - 1) subsets are empty (in one of the functions above). Then we would only need to check whether the first max set element is smaller or greater (see the theorem below).
        // However, by checking the max set just after extension (instead of checking it before next extension), we can spare one iteration of the algorithm.
    }

    // ## There can't be any *row gap* in the max set
    //
    // ### Statement
    //
    // Let's say we have M_X (a max set for class X) and there are no elements with column size n, where 1 <= n <= N, where N is the number of columns in the relation minus one (so that the largest possible size of a max set element is N). Then, at least one of the following is true:
    // 1. There are no elements in M_X with column size > n.
    // 2. There are no elements in M_X with column size < n.
    //
    // ### Proof
    //
    // Cases when n = 1 and n = N are trivial. Let's consider the case when 1 < n < N. For such n, we can define *lattice path*:
    // - Let e be a lattice element with size n. Then, its Pt(e) (*top path*) is any sequence of lattice elements with sizes n + 1, n + 2, ..., N, connected by the lattice edges. E.g., if e = AB, then one such path might be ABC, ABCD, ABCDE, ...
    // - Its Pb(e) (*bottom path*) is the same concept but in the opposite direction.
    // - Finally, P(e) is any union of any Pt(e), Pb(e), and the element e itself.
    // At most one element of each P(e) can be in M_X (otherwise, the smaller one would be a subset of the larger one, which is not allowed in max sets).
    //
    // For contradiction, let's assume there are M_X elements t and d, where t has size > n and d has size < n. Let's also define E to be the set of all lattice elements e with size n (i.e., the missing row).
    //
    // Now we can construct E_1, which will be the set of all elements e from E whose Pt(e) contains t. Each element e of E_1 has a path to t, therefore, each of its Pb(e) can't contain any M_X elements.
    // Let's consider some path P(e) that doesn't include t. Its Pt(e) must contain an M_X element (if it didn't, it would mean that its smallest element, the one with only one column (WLOG, let's call it Y), forms a functional dependency Y -> X, which is a contradiction to the fact that t, which is a superset of e and thus a superset of { Y }, is in M_X).
    // So, each Pt(e) of all elements in E_1 contains exactly one M_X element. Let's call the set of those M_X elements T_1.
    //
    // Now we can construct E_2, which will be the set of all elments from E whose Pt(e) contains any element from T_1. Then we can use the same arguments as above to construct T_2, E_3, T_3, ... Eventually, for some k, we will have E_k = E (see below why). At this point, we now that for each element e in E, its Pt(e) contains an M_X element, so, its Pb(e) can't contain any M_X elements. Which is a contradiction to the fact that d is in M_X.
    //
    // ### Proof that E_k = E
    //
    // WLOG, element ABC... is in E_1. For an arbitrary column Z that is not in ABC..., the set T_1 will have either ABC...Z or some of its supersets. Then, E_2 will have all subsets of ABC...Z that have size n, e.g., BC...Z, AC...Z, AB...Z, and so on. Thus, we have shown that For each element e in E_1, the set E_2 contains all elements that can be generated from e by replacing any of its column by an arbitrary different column.
    // With the same logic, E_3 will contain elements that can be generated from E_2 by changing one column, therefore also elements from E_1 with two columns changed.
    // So, E_n will have elements with all columns changed, i.e., all elements from E.

}
