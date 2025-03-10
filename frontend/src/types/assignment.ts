import { type ReferenceRow, type ExampleRow, type ExampleRelation, type ExampleState } from './armstrongRelation';
import { type DatasetData } from './dataset';
import { JobResult, type JobResultFromServer } from './jobResult';

export enum AssignmentVerdict {
    New = 'NEW',
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    DontKnow = 'I_DONT_KNOW',
}

export type AssignmentInfo = {
    id: string;
    verdict: AssignmentVerdict;
    rowIndex: number;
    // referenceRow: ReferenceRow;
    // exampleRow: ExampleRow;
};

export type AssignmentFromServer = AssignmentInfo & {
    owner: 'workflow' | 'worker';
    /** Either worfklowId or workerId. */
    ownerId: string;
    // discoveryResult: JobResultFromServer;
    // dataset: DatasetData;
    relation: ExampleRelation;

};
// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: string,
        readonly owner: 'workflow' | 'worker',
        readonly ownerId: string,
        readonly verdict: AssignmentVerdict,
        readonly isFinished: boolean,
        // readonly discoveryResult: JobResult,
        // readonly dataset: DatasetData,
        readonly relation: ExampleRelation,
    ) {}

    static fromServer(input: AssignmentFromServer): Assignment {
        return new Assignment(
            input.id,
            input.owner,
            input.ownerId,
            input.verdict,
            input.verdict !== AssignmentVerdict.New,
            // JobResult.fromServer(input.discoveryResult),
            // input.dataset,
            input.relation,
        );
    }
}

export type AssignmentInit = {
    /** If undefined, the workflow owner is assigning himself. */
    workerId: string | undefined;
    workflowId: string;
    rowIndex: number;
};

/** @deprecated */
export type NegativeExampleInfo = {
    id: string;
    state: ExampleState;
}
