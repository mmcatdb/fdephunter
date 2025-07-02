import { type ExampleRelation } from './armstrongRelation';

export enum AssignmentState {
    New = 'NEW',
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    DontKnow = 'DONT_KNOW',
}

export type AssignmentInfo = {
    id: string;
    state: AssignmentState;
    rowIndex: number;
};

export type AssignmentResponse = AssignmentInfo & {
    workflowId: string;
    relation: ExampleRelation;
    decision: ExampleDecision | undefined;
};

// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: string,
        readonly workflowId: string,
        readonly state: AssignmentState,
        readonly relation: ExampleRelation,
        readonly decision: ExampleDecision | undefined,
    ) {}

    static fromResponse(input: AssignmentResponse): Assignment {
        return new Assignment(
            input.id,
            input.workflowId,
            input.state,
            input.relation,
            input.decision,
        );
    }
}

export type ExampleDecision = {
    status: DecisionStatus;
    columns: {
        /** If undefined, the column isn't a part of the max set (so it should be ignored). */
        status: DecisionColumnStatus | undefined;
        reasons: string[];
    }[];
};

export enum DecisionStatus {
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    Unanswered = 'UNANSWERED',
}

export enum DecisionColumnStatus {
    Undecided = 'UNDECIDED',
    Valid = 'VALID',
    Invalid = 'INVALID',
}
