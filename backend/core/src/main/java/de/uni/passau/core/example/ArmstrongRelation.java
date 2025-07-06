package de.uni.passau.core.example;

import java.util.List;

public class ArmstrongRelation {

    /** Values of the reference row. */
    public String[] referenceRow;
    public List<ExampleRow> exampleRows;

    public ArmstrongRelation(String[] referenceRow, List<ExampleRow> exampleRows) {
        this.referenceRow = referenceRow;
        this.exampleRows = exampleRows;
    }

}
