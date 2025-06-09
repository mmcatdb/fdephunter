import { type ArmstrongRelation } from './armstrongRelation';

export type JobResultFromServer = {
    id: string;
    payload: string;
    relation: ArmstrongRelation;
};

export class JobResult {
    private constructor(
        readonly id: string,
        readonly relation: ArmstrongRelation,
    ) {}

    static fromServer(input: JobResultFromServer): JobResult {
        return new JobResult(
            input.id,
            input.relation,
        );
    }
}

type FDEdgeFromServer = {
    source: string;
    destination: string;
    weight: number;
    sourceAsList: string[];
};

type FDNodeFromServer = {
    label: string[];
    incomingEdges: FDEdgeFromServer[];
    weight: number;
};

export type FDPayloadFromServer = {
    vertices: Record<string, FDNodeFromServer>;
    rankedVertices: FDNodeFromServer[];
};
