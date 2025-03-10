import { type NegativeExampleInfo } from './assignment';

export enum WorkflowState {
    /**
     * Initial screen. The user is selecting dataset and algorithm.
     */
    InitialSettings = 'INITIAL',
    /**
     * The user confirmed the settings. A job is created for the FD discovery algorithm. The user gets back id of the job so he can watch the job state in real time.
     */
    WaitingForInitialFD = 'INITIAL_JOB_WAITING',
    /**
     * The user can see the FDs in multiple ways (list, graph).
     */
    NegativeExamples = 'WORKER_ASSIGNMENT',
    /**
     * The user requested new negative sample.
     * This step has two phases - first, the next vertex is selected (pickNext). Then the next negative sample is generated. However, we can probably merge them together.
     */
    //WaitingForNextSample = 'waitingForNextSample',
    /**
     * The negative sample is generated so we can show it to the user. He now has to decide whether it is truly a negative example. If yes, he should provide a reason.
     * Then, if there are still some unprocessed FDs, we move to the step DisplayFD.
     * Otherwise, we continue with the next step.
     */
    //EvaluatingSample = 'evaluatingSample',
    PositiveExamples = 'POSITIVE_EXAMPLES',
    /**
     * Very simillar to the step WaitingForInitialFD. However, there are two possible outcomes:
     * - New FDs are found and we move to the step DisplayFD.
     * - Nothing new to process so we continue.
     */
    // WaitingForFD = 'JOB_WAITING',
    /**
     * Yes! We have finally made it. The algorithm drops the final set of genuine FDs and we level up.
     */
    DisplayFinalFD = 'FINAL',
}

export type WorkflowFromServer = {
    id: string;
    state: WorkflowState;
    iteration: number;
};

export class Workflow {
    private constructor(
        readonly id: string,
        readonly state: WorkflowState,
        readonly iteration: number,
    ) {}

    static fromServer(input: WorkflowFromServer): Workflow {
        return new Workflow(
            input.id,
            input.state,
            input.iteration,
        );
    }
}

export type WorkflowStats = {
    FDsInitial: number;
    FDsRemaining: number;
    examplesPositive: number;
    examplesNegative: number;
    // examplesTotal = examplesPositive + examplesNegative
};

export type ClassFromServer = {
    id: string;
    label: string;
    weight: number;
    iteration: number;
    example?: NegativeExampleInfo;
};

export class Class {
    private constructor(
        readonly id: string,
        readonly label: string,
        readonly weight: number,
        readonly iteration: number,
        readonly example?: NegativeExampleInfo,
    ) {}

    static fromServer(input: ClassFromServer): Class {
        return new Class(
            input.id,
            input.label,
            input.weight,
            input.iteration,
            input.example,
        );
    }
}
