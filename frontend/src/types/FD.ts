import { type Edge, MarkerType, type Node } from '@xyflow/react';

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
