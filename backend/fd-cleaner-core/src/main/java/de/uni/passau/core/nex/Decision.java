package de.uni.passau.core.nex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;


/**
 * Represents the decision for a negative example.
 */
public class Decision {

    private Status status;
    private List<ColumnReason<String>> userReasons = new ArrayList<>();
    private List<ColumnReason<PredefinedReason>> predefinedReasons = new ArrayList<>();

    public static record Column(
        String name,
        List<String> userReasons,
        List<PredefinedReason> predefinedReasons
    ) {}

    public static record ColumnReason<T>(
        List<T> reasons,
        String columnName
    ) {}

    /**
     * Constructs a new decision with the given status and predefined reasons.
     *
     * @param status the status of the decision
     * @param predefinedReasons A list of tuples, with the predefined reason(s) for the decision and the column(s) that the reason(s) apply to
     * @param userReasons A list of tuples, with the user-provided reason for the decision and the column(s) that the reason(s) apply to
     *
     */
    public Decision(Status status, List<ColumnReason<PredefinedReason>> predefinedReasons, List<ColumnReason<String>> userReasons) {
        this.status = status;
        this.predefinedReasons = predefinedReasons;
        this.userReasons = userReasons;
    }

    public Decision(Status status, List<Column> columns) {
        this.status = status;
        columns.forEach(column -> {
            if (!column.predefinedReasons.isEmpty())
                predefinedReasons.add(new ColumnReason<>(column.predefinedReasons, column.name));

            if (!column.userReasons.isEmpty())
                userReasons.add(new ColumnReason<>(column.userReasons, column.name));
        });
    }

    /**
     * Constructs a new decision with default values.
     */
    public Decision() {
        this.status = Status.UNANSWERED;
    }

    /**
     * Returns the status of the decision.
     *
     * @return the status of the decision
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets the status of the decision.
     *
     * @param status the new status of the decision
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<String> getRejectedColumns() {
        var output = new TreeSet<String>();
        output.addAll(userReasons.stream().map(ColumnReason::columnName).toList());
        output.addAll(predefinedReasons.stream().map(ColumnReason::columnName).toList());

        return output;
    }

    /**
     * Returns the predefined reasons and corresponding columns for the decision.
     *
     * @return the predefined reasons for the decision and corresponding columns.
     */
    public List<ColumnReason<PredefinedReason>> getPredefinedReasons() {
        return predefinedReasons;
    }

    /**
     * Sets the predefined reasons and corresponding columns for the decision.
     *
     * @param predefinedReasons the list of predefined reasons for the decision and corresponding columns.
     */
    public void setPredefinedReasons(List<ColumnReason<PredefinedReason>> predefinedReasons) {
        this.predefinedReasons = predefinedReasons;
    }

    // /**
    // * Sets the predefined reason and corresponding columns for the decision.
    // *
    // * @param predefinedReasons the predefined reason for the decision and
    // *                          corresponding columns.
    // */
    // public void setPredefinedReasons(ColumnReason<PredefinedReason> predefinedReason) {
    //     this.predefinedReasons = new ArrayList<>();
    //     this.predefinedReasons.add(predefinedReason);
    // }

    /**
     * Adds a predefined reason and corresponding columns to the decision.
     *
     * @param predefinedReason the predefined reason to add and corresponding columns.
     */
    public void addPredefinedReason(ColumnReason<PredefinedReason> predefinedReason) {
        this.predefinedReasons.add(predefinedReason);
    }

    /**
     * Returns the user-provided reason and corresponding columns for the decision.
     *
     * @return the user-provided reason for the decision and corresponding columns.
     */
    public List<ColumnReason<String>> getuserReasons() {
        return userReasons;
    }

    /**
     * Sets the user-provided reasons and corresponding columns for the decision.
     *
     * @param userReasons the list of user-provided reason for the decision and corresponding columns.
     */
    public void setuserReasons(List<ColumnReason<String>> userReasons) {
        this.userReasons = userReasons;
    }

    /**
     * Adds a user-provided reason and corresponding columns for the decision.
     *
     * @param userReason the user-provided reason for the decision to be and corresponding columns.
     */
    public void adduserReason(ColumnReason<String> userReason) {
        this.userReasons.add(userReason);
    }

    /**
     * Returns the columns the user marked as problematic.
     */
    public List<String> getProblematicColumns() {
        List<String> problematicColumns = new ArrayList<>();
        if (this.predefinedReasons != null) {
            for (ColumnReason<PredefinedReason> reason : this.predefinedReasons) {
                problematicColumns.add(reason.columnName());
            }
        }
        if (this.userReasons != null) {
            for (ColumnReason<String> reason : this.userReasons) {
                problematicColumns.add(reason.columnName());
            }
        }
        return problematicColumns;
    }

    /**
     * Represents the status for a decision.
     */
    public enum Status {
        ACCEPTED,
        REJECTED,
        UNANSWERED,
    }

    /**
     * Represents the predefined reasons for a decision.
     */
    public enum PredefinedReason {
        VALUE_MUST_BE_UNIQUE_IN_COLUMNS, // -> Create a view without the column that must be unique
        VALUE_MUST_BE_UNIQUE_IN_ROW, // -> Choose different row, use random value or ask user to update
        VALUES_INDETIFY_EACH_OTHER, // What we mean here is that there is only on combination of multiple values
        // from different columns that is valid.
        // E.g., Zip code determines city and vice versa. -> Make sure to use column
        // values from same row.
        // If one of the columns is on RHS, might be a condition for canceling?
        VALUES_DO_NOT_MATCH, // -> (Ask the user to) change the value of the cell? Otherweise ignore and
        // assume the example was accepted
        VALUE_MUST_BE_IN_RANGE, // -> Ask user to define range and update example
        VALUE_DOES_NOT_MAKE_SENSE_AT_ALL, // -> Should only happen with (randomly) generated values. Unless we use a
        // dictionary to generate values, we can't do anything about it
    }

    public static final ObjectReader jsonReader = new ObjectMapper().readerFor(Decision.class);
    public static final ObjectWriter jsonWriter = new ObjectMapper().writerFor(Decision.class);

}
