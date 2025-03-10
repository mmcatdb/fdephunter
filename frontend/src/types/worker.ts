import { type AssignmentInfo } from './assignment';
import { User, type UserFromServer } from './user';

export enum WorkerState {
    Pending = 'PENDING', // The expert hasn't yet accepted nor rejected the assignment.
    Unsubscribed = 'UNSUBSCRIBED', // The expert has rejected the assignment.
    Idle = 'IDLE', // The expert has accepted the assignment and now waits for the next negative example.
    Assigned = 'ASSIGNED',
    Unavailable = 'UNAVAILABLE',
}

export type WorkerFromServer = {
    id: string; // The worker's id
    state: WorkerState;
    user: UserFromServer;
    assignment?: AssignmentInfo;
    // nex?: NegativeExampleFromServer; // Negative example node
    //isStopped: boolean;
    //stats: number;
    //lastAssignmentId?: string;
};

export class Worker {
    private constructor(
        readonly id: string,
        readonly state: WorkerState,
        readonly user: User,
        //readonly isStopped: boolean,
        //readonly stats: number, // How many examples he solved
        //readonly lastAssignmentId?: string,
        readonly assignment?: AssignmentInfo,
    ) {}

    static fromServer(input: WorkerFromServer): Worker {
        return new Worker(
            //input.id,
            input.id,
            input.state,
            User.fromServer(input.user),
            //input.isStopped,
            //input.stats,
            input.assignment,// && input.nex && Assignment.fromServer({ assignment: input.assignment, nex: input.nex }),
        );
    }
}

export type WorkerInit = {
    workflowId: string;
    user: string;
};
