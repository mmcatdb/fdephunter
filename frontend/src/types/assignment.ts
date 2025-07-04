import { ExampleRelation, type ExampleRelationResponse } from './armstrongRelation';
import { type Id } from './id';

export type AssignmentResponse = {
    id: Id;
    workflowId: Id;
    relation: ExampleRelationResponse;
};

// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: Id,
        readonly workflowId: Id,
        readonly relation: ExampleRelation,
    ) {}

    static fromResponse(input: AssignmentResponse): Assignment {
        return new Assignment(
            input.id,
            input.workflowId,
            ExampleRelation.fromResponse(input.relation),
        );
    }
}
