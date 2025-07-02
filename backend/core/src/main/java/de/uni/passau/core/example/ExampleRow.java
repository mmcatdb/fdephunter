package de.uni.passau.core.example;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class ExampleRow {

    public String[] values;
    /** The indexes of the columns that form the max set element. */
    public ColumnSet maxSetElement;
    /** Whether it is a negative or positive example. */
    public boolean isPositive;

    /** If null, the row is still undecided. */
    public @Nullable ExampleDecision decision;

}
