import { DateTime } from 'luxon';
import { type Id } from './id';

export enum JobState {
    Waiting = 'WAITING',
    Running = 'RUNNING',
    Finished = 'FINISHED',
    Failed = 'FAILED',
}

export type JobResponse = {
    id: Id;
    state: JobState;
    description: string;
    iteration: number;
    /** In UTC. */
    startedAt?: string;
    /** In UTC. */
    finishedAt?: string;
    // resultId?: string;
};

export class Job {
    private constructor(
        readonly id: Id,
        readonly state: JobState,
        readonly description: string,
        readonly iteration: number,
        readonly startedAt: DateTime | undefined,
        readonly finishedAt: DateTime | undefined,
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
            input.startedAt ? DateTime.fromISO(input.startedAt) : undefined,
            input.finishedAt ? DateTime.fromISO(input.finishedAt) : undefined,
            progress,
        );
    }
}

const progressCache = new Map<string, number>();

// FIXME Implement on the backend later.

/** @returns A number from 0 to 1. Or NaN for error. */
function computeNextProgress(prev: number, state: JobState): number {
    if (state === JobState.Finished)
        return 1;
    if (state === JobState.Waiting)
        return 0;
    if (state === JobState.Failed)
        return NaN;

    const range = (1 - prev) / 2;
    const next = Math.random() * range + prev;

    return next;
}
