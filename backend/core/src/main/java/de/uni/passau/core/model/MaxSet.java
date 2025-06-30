package de.uni.passau.core.model;

import java.util.List;

/**
 * Contains all maximal set elements for given class.
 * Each element (represented by {@link ColumnSet}) is a maximal set of columns such that when put on LHS, it doesn't form a functional dependency (if the given class is on RHS).
 */
public class MaxSet {

    /** Index of the RHS column. */
    public final int forClass;
    /** Max set elements in ascending order. */
    public final List<ColumnSet> elements;

    public MaxSet(int forClass, List<ColumnSet> elements) {
        this.forClass = forClass;
        this.elements = elements;
    }

}
