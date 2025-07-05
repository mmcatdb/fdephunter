package de.uni.passau.algorithms.maxset;

import de.uni.passau.core.model.ColumnSet;

/** This class is just a {@link ColumnSet}, but with a different name so that it's easier to distinguish its purpose in the algorithms. */
public class AgreeSet extends ColumnSet {

    public AgreeSet() {
        super();
    }

    public ColumnSet cloneColumnSet() {
        return clone();
    }

}
