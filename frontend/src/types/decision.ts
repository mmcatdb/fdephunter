export enum DecisionStatus {
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    Unanswered = 'UNANSWERED',
}

export type DecisionFromServer = {
    id: string;
    status: DecisionStatus;
    reasons: DecisionReason[];
}

export type DecisionReason = //'NONE'
    | 'VALUE_MUST_BE_UNIQUE_IN_COLUMNS'
    | 'VALUE_MUST_BE_UNIQUE_IN_ROW'
    | 'VALUES_INDETIFY_EACH_OTHER'
    | 'VALUES_DO_NOT_MATCH'
    | 'VALUE_MUST_BE_IN_RANGE'
    | 'VALUE_DOES_NOT_MAKE_SENSE_AT_ALL';

export class Decision {
    private constructor(
        readonly id: string,
        readonly status: DecisionStatus,
        readonly reasons: DecisionReason[],
    ) {}

    static fromServer(input: DecisionFromServer): Decision {
        return new Decision(
            input.id,
            input.status,
            input.reasons,
        );
    }
}

export type DecisionInit = {
    status: DecisionStatus;
    columns: {
        name: string;
        reasons: string[];
    }[];
};

export type EvaluationResult = {
    decision: DecisionFromServer;
    nextAssignmentId: string;
}
