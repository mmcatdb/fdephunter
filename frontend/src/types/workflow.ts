import { type Id } from './id';

export enum WorkflowState {
    /**
     * Initial screen. The user is selecting dataset and algorithm.
     */
    InitialSettings = 'INITIAL_SETTINGS',
    /**
     * The user confirmed the settings. A job is created for the FD discovery algorithm. The user gets back id of the job so he can watch the job state in real time.
     */
    InitialFdDiscovery = 'INITIAL_FD_DISCOVERY',
    /**
     * The user can see the FDs in multiple ways (list, graph).
     */
    NegativeExamples = 'NEGATIVE_EXAMPLES',
    /**
     * The negative sample is generated so we can show it to the user. He now has to decide whether it is truly a negative example. If yes, he should provide a reason.
     * Then, if there are still some unprocessed FDs, we move to the step DisplayFD.
     * Otherwise, we continue with the next step.
     */
    PositiveExamples = 'POSITIVE_EXAMPLES',
    /**
     * Yes! We have finally made it. The algorithm drops the final set of genuine FDs and we level up.
     */
    DisplayFinalFds = 'FINAL',
}

export type WorkflowResponse = {
    id: Id;
    state: WorkflowState;
    iteration: number;
    /** Is set after the dataset is uploaded so it's not known from the start. */
    datasetId: Id | null;
};

export class Workflow {
    private constructor(
        readonly id: Id,
        readonly state: WorkflowState,
        readonly iteration: number,
        readonly datasetId: Id | undefined,
    ) {}

    static fromResponse(input: WorkflowResponse): Workflow {
        return new Workflow(
            input.id,
            input.state,
            input.iteration,
            input.datasetId ?? undefined,
        );
    }
}

export type WorkflowStats = {
    FdsInitial: number;
    FdsRemaining: number;
    examplesPositive: number;
    examplesNegative: number;
    // examplesTotal = examplesPositive + examplesNegative
};
