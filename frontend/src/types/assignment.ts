import { type DatasetData } from './dataset';
import { JobResult, type JobResultFromServer } from './jobResult';
import { NegativeExample, type NegativeExampleFromServer } from './negativeExample';

export enum AssignmentVerdict {
    New = 'NEW',
    Accepted = 'ACCEPTED',
    Rejected = 'REJECTED',
    DontKnow = 'I_DONT_KNOW',
}

export type AssignmentInfo = {
    id: string;
    verdict: AssignmentVerdict;
};

export type AssignmentFromServer = AssignmentInfo & {
    expertId: string;
    example: NegativeExampleFromServer;
    discoveryResult: JobResultFromServer;
    dataset: DatasetData;
};

export class Assignment {
    private constructor(
        readonly id: string,
        readonly workerId: string,
        readonly verdict: AssignmentVerdict,
        readonly isFinished: boolean,
        readonly example: NegativeExample,
        readonly discoveryResult: JobResult,
        readonly dataset: DatasetData,
    ) {}

    static fromServer(input: AssignmentFromServer): Assignment {
        console.log(input);
        return new Assignment(
            input.id,
            input.expertId,
            input.verdict,
            input.verdict !== AssignmentVerdict.New,
            NegativeExample.fromServer(input.example),
            JobResult.fromServer(input.discoveryResult),
            input.dataset,
        );
    }
}

export type AssignmentInit = {
    workerId: string;
};