import { type Edge, MarkerType, type Node } from '@xyflow/react';
import { type FDPayloadFromServer } from './jobResult';

export type FDGraph = {
    nodes: FDNode[];
    edges: FDEdge[];
};

type FDNode = {
    id: string;
    label: string;
    columns: string[];
};

export type FDEdge = {
    readonly id: string;
    readonly source: FDNode;
    readonly target: FDNode;
};

export function createFDGraph(payload: FDPayloadFromServer): FDGraph {
    const nodeMap = createNodes(payload);
    const edges = createEdges(payload, nodeMap);

    return {
        nodes: [ ...nodeMap.values() ],
        edges,
    };
}

function createNodes(payload: FDPayloadFromServer): Map<string, FDNode> {
    const nodeMap = new Map<string, FDNode>();

    Object.entries(payload.vertices).forEach(([ labelList, nodeFromServer ], index) => {
        const node: FDNode = {
            id: '' + index,
            label: labelList.replace(/,/g, '\n'),
            columns: nodeFromServer.label,
        };
        nodeMap.set(labelList, node);
    });

    return nodeMap;
}

function createEdges(payload: FDPayloadFromServer, nodes: Map<string, FDNode>): FDEdge[] {
    const edges: FDEdge[] = [];

    Object.values(payload.vertices).forEach(nodeFromServer => {
        nodeFromServer.incomingEdges.forEach(edgeFromServer => {
            const source = nodes.get(edgeFromServer.source);
            if (!source)
                throw new Error(`Node ${edgeFromServer.source} not found`);

            const target = nodes.get(edgeFromServer.destination);
            if (!target)
                throw new Error(`Node ${edgeFromServer.destination} not found`);

            const edge: FDEdge = {
                id: `${source.id}_${target.id}`,
                source,
                target,
            };

            edges.push(edge);
        });
    });

    return edges;
}

export type RFNode = Node<{ label: string }>;
export type RFEdge = Edge;

export type RFGraph = {
    nodes: RFNode[];
    edges: RFEdge[];
};

export const NODE_OPTIONS: Omit<RFNode, 'id' | 'position' | 'data'> = {
    draggable: false,
    connectable: false,
    selectable: false,
    className: 'whitespace-pre-line',
};

export const EDGE_OPTIONS: Omit<RFEdge, 'id' | 'source' | 'target'> = {
    //animated: true,
    markerEnd: {
        width: 30,
        height: 30,
        type: MarkerType.Arrow,
    },
};
