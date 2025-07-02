import { DateTime } from 'luxon';
import { type ArmstrongRelation } from './armstrongRelation';

export enum JobState {
    Waiting = 'WAITING',
    Running = 'RUNNING',
    Pending = 'PENDING',
    Finished = 'FINISHED',
}

export type JobResponse = {
    id: string;
    state: JobState;
    description: string;
    iteration: number;
    /** In UTC. */
    startedAt: string;
    resultId?: string;
};

export class Job {
    private constructor(
        readonly id: string,
        readonly state: JobState,
        readonly description: string,
        readonly iteration: number,
        readonly startedAt: DateTime,
        readonly progress: number, // from 0 to 1
    ) {}

    static fromResponse(input: JobResponse): Job {
        const prevProgress = progressCache.get(input.id) ?? 0;
        const progress = computeNextProgress(prevProgress, input.state);
        progressCache.set(input.id, progress);

        return new Job(
            input.id,
            input.state,
            input.description,
            input.iteration,
            DateTime.fromISO(input.startedAt),
            progress,
        );
    }
}

const progressCache = new Map<string, number>();

// FIXME Implement on the backend later.

/** @returns A number from 0 to 1. */
function computeNextProgress(prev: number, state: JobState): number {
    if (state === JobState.Finished)
        return 1;
    if (state === JobState.Waiting || state === JobState.Pending)
        return 0;

    const range = (1 - prev) / 2;
    const next = Math.random() * range + prev;

    return next;
}

export type JobResultResponse = {
    id: string;
    payload: string;
    relation: ArmstrongRelation;
};

export class JobResult {
    private constructor(
        readonly id: string,
        readonly relation: ArmstrongRelation,
    ) {}

    static fromResponse(input: JobResultResponse): JobResult {
        return new JobResult(
            input.id,
            input.relation,
        );
    }
}
