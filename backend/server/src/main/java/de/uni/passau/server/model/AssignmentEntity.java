package de.uni.passau.server.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import de.uni.passau.core.example.ExampleRow;

@Document("assignment")
public class AssignmentEntity {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public UUID workflowId;

    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /** Values of the reference row. */
    public String[] referenceRow;
    /** The example relation contains exactly one example row. */
    public ExampleRow exampleRow;

    public AssignmentEntity(UUID id, UUID workflowId, String[] columns, String[] referenceRow, ExampleRow exampleRow) {
        this.id = id;
        this.workflowId = workflowId;
        this.columns = columns;
        this.referenceRow = referenceRow;
        this.exampleRow = exampleRow;
    }

    public static AssignmentEntity create(UUID workflowId, String[] columns, String[] referenceRow, ExampleRow exampleRow) {
        return new AssignmentEntity(
            UUID.randomUUID(),
            workflowId,
            columns,
            referenceRow,
            exampleRow
        );
    }

}
