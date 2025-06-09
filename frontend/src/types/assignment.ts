import { type ExampleRelation, type ExampleState } from './armstrongRelation';
import { type DecisionInit } from './decision';

export enum AssignmentVerdict {
    New = 'NEW',
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    DontKnow = 'DONT_KNOW',
}

export type AssignmentInfo = {
    id: string;
    verdict: AssignmentVerdict;
    rowIndex: number;
};

export type AssignmentFromServer = AssignmentInfo & {
    workflowId: string;
    relation: ExampleRelation;
    decision: DecisionInit | undefined;
};

// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: string,
        readonly workflowId: string,
        readonly verdict: AssignmentVerdict,
        readonly isFinished: boolean,
        readonly relation: ExampleRelation,
        readonly decision: DecisionInit | undefined,
    ) {}

    static fromServer(input: AssignmentFromServer): Assignment {
        return new Assignment(
            input.id,
            input.workflowId,
            input.verdict,
            input.verdict !== AssignmentVerdict.New,
            input.relation,
            input.decision,
        );
    }
}

export type AssignmentInit = {
    workflowId: string;
    rowIndex: number;
};

/** @deprecated */
export type NegativeExampleInfo = {
    id: string;
    state: ExampleState;
}
