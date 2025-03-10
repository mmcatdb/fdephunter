import { type ArmstrongRelation } from './armstrongRelation';
import { type FDGraph, createFDGraph } from './FD';

export type JobResultFromServer = {
    id: string;
    payload: string;
    relation: ArmstrongRelation;
};

export class JobResult {
    private constructor(
        readonly id: string,
        // readonly fdGraph: FDGraph,
        readonly relation: ArmstrongRelation,
    ) {}

    static fromServer(input: JobResultFromServer): JobResult {
        // const payload = JSON.parse(input.payload) as FDPayloadFromServer;
        // const fdGraph = createFDGraph(payload);

        return new JobResult(
            input.id,
            // fdGraph,
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
    // labelList: string[];
};

export type FDPayloadFromServer = {
    vertices: Record<string, FDNodeFromServer>;
    rankedVertices: FDNodeFromServer[];
};
