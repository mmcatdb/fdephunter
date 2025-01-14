import { DateTime } from 'luxon';

export type ExecuteRediscoveryParams = {
    approach: string;
};

export type ExecuteDiscoveryParams = ExecuteRediscoveryParams & {
    datasets: string[];
    approach: string;
    description: string;
};

export enum JobState {
    Waiting = 'WAITING',
    Running = 'RUNNING',
    Pending = 'PENDING',
    Finished = 'FINISHED',
}

export type JobFromServer = {
    id: string;
    state: JobState;
    startDate: DateTime;
    progress: number;
};

export class Job {
    private constructor(
        readonly id: string,
        readonly state: JobState,
        readonly startDate: DateTime,
        readonly progress: number, // from 0 to 1
    ) {}

    static fromServer(input: JobFromServer): Job {
        return new Job(
            input.id,
            input.state,
            DateTime.now(), // TODO
            Math.random(), // TODO
        );
    }
}
