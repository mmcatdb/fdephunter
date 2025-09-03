import { ExampleRow, type ExampleRowResponse } from './examples';
import { type Id } from './id';

export type AssignmentResponse = {
    id: Id;
    workflowId: Id;
    columns: string[];
    referenceRow: string[];
    exampleRow: ExampleRowResponse;
};

export class Assignment {
    private constructor(
        readonly id: Id,
        readonly workflowId: Id,
        /** Names of the columns. They are expected to be unique. */
        readonly columns: string[],
        /** Values of the reference row. */
        readonly referenceRow: string[],
        readonly exampleRow: ExampleRow,
    ) {}

    static fromResponse(input: AssignmentResponse): Assignment {
        return new Assignment(
            input.id,
            input.workflowId,
            input.columns,
            input.referenceRow,
            ExampleRow.fromResponse(input.exampleRow),
        );
    }
}
