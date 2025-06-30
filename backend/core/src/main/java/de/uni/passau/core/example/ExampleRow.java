package de.uni.passau.core.example;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class ExampleRow {

    String[] values;
    /** The indexes of the columns that form the max set element. */
    ColumnSet maxSetElement;
    /** Whether it is a negative or positive example. */
    boolean isPositive;

    /** If null, the row is still undecided. */
    @Nullable ExampleDecision decision;

}
