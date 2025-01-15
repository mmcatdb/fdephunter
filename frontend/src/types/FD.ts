import { type Edge, MarkerType, type Node } from 'reactflow';
import dagre from 'dagre';
import { type FDPayloadFromServer } from './jobResult';

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

export type FDGraph = {
    nodes: FDNode[];
    edges: FDEdge[];
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

const DEFAULT_NODE_DIMENSIONS = {
    width: 200,
    height: 50,
};

type RFNode = Node<{ label: string }>;
type RFEdge = Edge;

type RFGraph = {
    nodes: RFNode[];
    edges: RFEdge[];
};

export function createRFGraph(fdGraph: FDGraph): RFGraph {
    const graph = new dagre.graphlib.Graph();

    /*
    graph.setGraph({
        width: 700,
        height: 200,
    });
    */

    graph.setGraph({});

    graph.setDefaultEdgeLabel(function () {
        return {};
    });

    fdGraph.nodes.forEach(fdNode => graph.setNode(fdNode.id, { label: fdNode.label, ...DEFAULT_NODE_DIMENSIONS }));
    fdGraph.edges.forEach(fdEdge => graph.setEdge(fdEdge.source.id, fdEdge.target.id));

    dagre.layout(graph);

    const nodes: RFNode[] = graph.nodes().map(name => {
        const node = graph.node(name);

        return {
            id: name,
            position: {
                x: node.x,
                y: node.y,
            },
            data: {
                label: node.label!,
            },
            ...NODE_OPTIONS,
        };
    });

    const edges: RFEdge[] = graph.edges().map(edge => ({
        id: `${edge.v}_${edge.w}`,
        source: edge.v,
        target: edge.w,
        ...EDGE_OPTIONS,
    }));

    return {
        nodes,
        edges,
    };
}

const NODE_OPTIONS: Omit<RFNode, 'id' | 'position' | 'data'> = {
    draggable: false,
    connectable: false,
    className: 'whitespace-pre-line',
};

const EDGE_OPTIONS: Omit<RFEdge, 'id' | 'source' | 'target'> = {
    //animated: true,
    markerEnd: {
        width: 30,
        height: 30,
        type: MarkerType.Arrow,
    },
};
