package de.uni.passau.algorithms;

import java.util.ArrayList;

import de.uni.passau.algorithms.exception.ExtendMaxSetException;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;

/**
 * An opposite process to {@link ExtendMaxSets}. Moves positive examples from the confirmed elements to the candidates of the max sets.
 */
public class ContractMaxSets {

    public static MaxSets run(
        MaxSets maxSets,
        MaxSets initialMaxSets,
        int lhsSize
    ) {
        try {
            final var algorithm = new ContractMaxSets(maxSets, initialMaxSets, lhsSize);
            return algorithm.innerRun();
        }
        catch (final Exception e) {
            throw ExtendMaxSetException.inner(e);
        }
    }

    private final MaxSets maxSets;
    private final MaxSets initialMaxSets;
    private final int lhsSize;

    private ContractMaxSets(MaxSets maxSets, MaxSets initialMaxSets, int lhsSize) {
        this.maxSets = maxSets;
        this.initialMaxSets = initialMaxSets;
        this.lhsSize = lhsSize;
    }

    private MaxSets innerRun() {
        // Now we need to find all max set elements that have a size of lhsSize and are in the initial max sets.
        // We will move them to the candidates of the current max sets. So, we are not extending the max sets, quite the opposite - we try to decrease them by marking some elements as candidates.
        // Later, during the evaluation, if they are rejected, we will remove them from the candidates and move their subsets to the candidates as well.

        final MaxSets contractedMaxSets = new MaxSets(new ArrayList<>());

        for (final MaxSet original : maxSets.sets()) {
            if (original.isFinished()) {
                // If the max set is already finished, we don't need to contract it.
                contractedMaxSets.sets().add(original);
                continue;
            }

            // Clone the original max set because we don't want to modify it.
            final MaxSet contracting = original.clone();

            contractMaxSet(contracting);

            if (iscontractedMaxSetFinished(contracting))
                contracting.setIsFinished(true);

            contractedMaxSets.sets().add(contracting);
        }

        return contractedMaxSets;
    }

    private void contractMaxSet(MaxSet contracting) {
        initialMaxSets.sets().get(contracting.forClass).elements()
            // If the contracting max set has this initial set as confirmed, we move it to candidates.
            .filter(initial -> initial.size() == lhsSize && contracting.hasConfirmed(initial))
            .forEach(initial -> contracting.moveConfirmedToCandidates(initial));
    }

    private boolean iscontractedMaxSetFinished(MaxSet contracting) {
        // The max set is finished if it has no candidates and no initial elements that could be moved to candidates in later iterations.
        if (contracting.candidateCount() > 0)
            return false;

        for (final var initial : initialMaxSets.sets().get(contracting.forClass).confirmedElements())
            if (initial.size() <= lhsSize && contracting.hasConfirmed(initial))
                return false;

        return true;
    }

}
