import { type DatasetData, type DatasetHeader, type DatasetRow } from './dataset';

export enum NegativeExampleState {
    New = 'NEW',
    Rejected = 'REJECTED',
    Accepted = 'ACCEPTED',
    Answered = 'ANSWERED',
    Conflict = 'CONFLICT',
}

export type NegativeExampleFromServer = {
    id: string;
    payload: string;
    state: NegativeExampleState;
    dataset: DatasetData;
};

export class NegativeExample {
    private constructor(
        readonly id: string,
        readonly state: NegativeExampleState,
        readonly data: Record<string, string>,
    ) {}

    static fromServer(input: NegativeExampleFromServer): NegativeExample {
        const payload = JSON.parse(input.payload) as PayloadFromServer;

        return new NegativeExample(
            input.id,
            input.state,
            payload.values,
        );
    }

    toRow(header: DatasetHeader): DatasetRow {
        return header.map(column => this.data[column]);
    }
}

type PayloadFromServer = {
    values: Record<string, string>;
    // TODO some other stuff
};

export type NegativeExampleInfo = {
    id: string;
    state: NegativeExampleState;
}