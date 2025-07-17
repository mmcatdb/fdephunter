package de.uni.passau.core.example;

import java.util.List;

import de.uni.passau.core.model.ColumnSet;

public class ArmstrongRelation {

    /** Values of the reference row. */
    public String[] referenceRow;
    /** For testing and other reasons, the rows are ordered by their lhs (see {@link ColumnSet#compareTo}) */
    public List<ExampleRow> exampleRows;

    /**
     * Make sure to order the example rows before passing them to this constructor.
     */
    public ArmstrongRelation(String[] referenceRow, List<ExampleRow> exampleRows) {
        this.referenceRow = referenceRow;
        this.exampleRows = exampleRows;
    }

    public String tableToString() {
        final var sb = new StringBuilder();
        for (int i = 0; i < referenceRow.length; i++)
            sb.append(referenceRow[i]).append(", ");
        if (referenceRow.length > 0)
            sb.setLength(sb.length() - 2); // Remove the last comma and space

        for (final ExampleRow exampleRow : exampleRows) {
            sb.append("\n");
            for (int i = 0; i < exampleRow.values.length; i++)
                sb.append(exampleRow.values[i]).append(", ");
            if (exampleRow.values.length > 0)
                sb.setLength(sb.length() - 2); // Remove the last comma and space
        }

        return sb.toString();
    }

    public String exampleFdsToString() {
        final var sb = new StringBuilder();
        for (final ExampleRow exampleRow : exampleRows) {
            sb.append(exampleRow.lhsSet)
                .append(" -> ")
                .append(exampleRow.rhsSet)
                .append("\n");
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1); // Remove the last newline

        return sb.toString();
    }

}
