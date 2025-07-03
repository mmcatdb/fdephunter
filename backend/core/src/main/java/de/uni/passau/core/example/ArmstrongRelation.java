package de.uni.passau.core.example;

import java.util.List;

public class ArmstrongRelation {

    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /** Values of the reference row. */
    public String[] referenceRow;
    public List<ExampleRow> exampleRows;

    /**
     * Whether we are evaluating the positive examples or the negative ones.
     * Should be true if and only if all negative examples are already evaluated.
     */
    public boolean isEvaluatingPositives;

}
