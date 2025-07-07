package de.uni.passau.core.model;

import java.util.ArrayList;
import java.util.List;

public class ComplementMaxSet {

    /** Index of the RHS column. */
    public final int forClass;
    /** Max set elements in a random order. */
    public final List<ColumnSet> elements = new ArrayList<>();

    public ComplementMaxSet(int forClass) {
        this.forClass = forClass;
    }

    public void addElement(ColumnSet element) {
        elements.add(element);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("cmax(").append(forClass).append(": ");
        for (final ColumnSet set : elements)
            sb.append(set).append(", ");
        if (elements.size() > 0)
            sb.setLength(sb.length() - 2); // Remove last comma and space.
        sb.append(")");

        return sb.toString();
    }

}
