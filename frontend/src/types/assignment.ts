import { MOCK_ARMSTRONG_RELATION, type ReferenceRow, type ExampleRow, type ExampleRelation } from './armstrongRelation';
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
    referenceRow: ReferenceRow;
    exampleRow: ExampleRow;
};

export type AssignmentFromServer = AssignmentInfo & {
    expertId: string;
    example: NegativeExampleFromServer;
    discoveryResult: JobResultFromServer;
    dataset: DatasetData;
};

// TODO Replace by a simple type (if possible).
export class Assignment {
    private constructor(
        readonly id: string,
        readonly workerId: string,
        readonly verdict: AssignmentVerdict,
        readonly isFinished: boolean,
        /** @deprecated Use exampleRelation. */
        readonly example: NegativeExample,
        readonly discoveryResult: JobResult,
        readonly dataset: DatasetData,
        readonly exampleRelation: ExampleRelation,
    ) {}

    static fromServer(input: AssignmentFromServer): Assignment {
        return new Assignment(
            input.id,
            input.expertId,
            input.verdict,
            input.verdict !== AssignmentVerdict.New,
            NegativeExample.fromServer(input.example),
            JobResult.fromServer(input.discoveryResult),
            input.dataset,
            {
                columns: MOCK_ARMSTRONG_RELATION.columns,
                referenceRow: MOCK_ARMSTRONG_RELATION.referenceRow,
                exampleRow: MOCK_ARMSTRONG_RELATION.exampleRows[3],
            },
        );
    }
}

export type AssignmentInit = {
    workerId: string;
};
