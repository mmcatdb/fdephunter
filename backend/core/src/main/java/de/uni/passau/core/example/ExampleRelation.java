package de.uni.passau.core.example;

/**
 * An example consists of a reference row and an example row. The two rows form a "relation".
 */
public class ExampleRelation {

    /** Names of the columns. They are expected to be unique. */
    String[] columns;
    /** Values of the reference row. */
    String[] referenceRow;
    /** The example relation contains exactly one example row. */
    ExampleRow exampleRow;

    public static class ExampleRow {

        String[] values;
        /** The indexes of the columns that form the maximal set. */
        int[] maxSet;
        /** Whether it is a negative or positive example. */
        boolean isPositive;
        /** A state of the evaluation of the whole example. */
        ExampleState state;

    }

    public enum ExampleState {
        NEW,
        REJECTED,
        ACCEPTED,
        UNDECIDED,
    }

}
