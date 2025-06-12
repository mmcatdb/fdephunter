package de.uni.passau.core.example;

import de.uni.passau.core.example.ExampleRelation.ExampleRow;

public class ArmstrongRelation {

    /** Names of the columns. They are expected to be unique. */
    String[] columns;
    /** Values of the reference row. */
    String[] referenceRow;
    ExampleRow[] exampleRows;

    /**
     * Whether we are evaluating the positive examples or the negative ones.
     * Should be true if and only if all negative examples are already evaluated.
     */
    boolean isEvaluatingPositives;

    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    int minimalFDs;
    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    int otherFDs;
    /** @deprecated This probably shouldn't be here, but we need it now for the stats. */
    int lhsSize;

}
