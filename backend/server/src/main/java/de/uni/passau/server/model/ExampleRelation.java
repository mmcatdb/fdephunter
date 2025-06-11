package de.uni.passau.server.model;

public class ExampleRelation {

    /** Names of the columns. They are expected to be unique. */
    String[] columns;
    /** Values of the reference row. */
    String[] referenceRow;

    public static class ExampleRow {

        String[] values;
        /** The indexes of the columns that form the maximal set. */
        int[] maxSet;
        /** Whether it is a negative or positive example. */
        boolean isPositive;
        ExampleState state;

    }

    public enum ExampleState {
        NEW,
        REJECTED,
        ACCEPTED,
        UNDECIDED,
    }

}
