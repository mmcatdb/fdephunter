package de.uni.passau.core.example;

import de.uni.passau.core.approach.FDInit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a negative example, which is a row in a database or CSV file.
 * A negative example identifies individual cells by their column name. Users may reject or accept a negative example,
 * and the decision is stored in the assignment. Users can also provide a reason for rejecting a negative example.
 * @deprecated
 */
public class NegativeExample implements Comparable<NegativeExample> {

    private static final String VIEW_NULL = "##VIEW_NULL"; //TODO: define globally -- also used in OurApproachAlgorithm

    public final String id;
    public final String previousIterationId;
    public final Map<String, String> innerValues;
    /** The original tuple the negative example is based on */
    public final Map<String, String> originalValues;
    /** The list of column names that should be visible to the user. */
    public final List<String> view;
    public final List<FDInit> fds;

    public NegativeExample(String id, @Nullable String previousIterationId, Map<String, String> innerValues, Map<String, String> originalValues, List<String> view, List<FDInit> fds) {
        this.id = id;
        this.previousIterationId = previousIterationId;
        this.innerValues = innerValues;
        this.originalValues = originalValues;
        this.view = view;
        this.fds = fds;
    }

    /**
     * Returns the values of masked by the view.
     */
    public Map<String, String> getValues() {
        final Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, String> entry : this.innerValues.entrySet()) {
            final String columnName = entry.getKey();
            final String value = this.view.contains(columnName) ? entry.getValue() : VIEW_NULL;
            values.put(columnName, value);
        }

        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NegativeExample {");
        sb.append("innerValues=").append(innerValues);
        sb.append(", view=").append(view);
        sb.append(", fds=").append(fds);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(NegativeExample other) {
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NegativeExample example && id.equals(example.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
