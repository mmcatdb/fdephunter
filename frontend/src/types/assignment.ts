import { type ExampleRelation, type ExampleState } from './armstrongRelation';

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
    decision: AssignmentDecision | undefined;
};

// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: string,
        readonly workflowId: string,
        readonly state: AssignmentState,
        readonly isFinished: boolean,
        readonly relation: ExampleRelation,
        readonly decision: AssignmentDecision | undefined,
    ) {}

    static fromServer(input: AssignmentResponse): Assignment {
        return new Assignment(
            input.id,
            input.workflowId,
            input.state,
            input.state !== AssignmentState.New,
            input.relation,
            input.decision,
        );
    }
}

export type AssignmentInit = {
    workflowId: string;
    rowIndex: number;
};

export type AssignmentDecision = {
    status: DecisionStatus;
    columns: {
        name: string;
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
