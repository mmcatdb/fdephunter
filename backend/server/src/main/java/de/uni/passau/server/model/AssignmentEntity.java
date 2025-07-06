package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uni.passau.core.example.ExampleRow;

@Document("assignment")
public class AssignmentEntity {

    @Id @JsonProperty("id")
    private UUID _id;

    public UUID id() {
        return _id;
    }

    public UUID workflowId;

    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /** Values of the reference row. */
    public String[] referenceRow;
    /** The example relation contains exactly one example row. */
    public ExampleRow exampleRow;

    // Rejected can be (most likely) removed from the database. However, the time and memory saving is negligible (each such row corresponds to an user's decision).
    // All new rows are active by default.
    /**
     * Whether the row is a part of the currently displayed Armstrong relation or not. We still want to keep the row in the database.
     * If a row is rejected, it becomes inactive. It can never come back.
     * If a row is make obsolete by a new row, it becomes inactive, but it can be made active again if the new row is rejected.
     */
    public boolean isActive = true;

    public static AssignmentEntity create(UUID workflowId, String[] columns, String[] referenceRow, ExampleRow exampleRow) {
        final var assignment = new AssignmentEntity();

        assignment._id = UUID.randomUUID();
        assignment.workflowId = workflowId;
        assignment.columns = columns;
        assignment.referenceRow = referenceRow;
        assignment.exampleRow = exampleRow;

        return assignment;
    }

}
