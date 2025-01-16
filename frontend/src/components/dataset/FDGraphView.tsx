import { useMemo } from 'react';
import type { FDGraph } from '@/types/FD';
import { createRFGraph, type FDGraph as FDGraphType } from '@/types/FD';
import ReactFlow from 'reactflow';
import 'reactflow/dist/style.css';

type FDGraphViewProps = {
    graph?: FDGraph;
};

export function FDGraphView({ graph }: FDGraphViewProps) {
    if (!graph)
        return null;

    return (
        <FDGraphDisplay graph={graph} />
    );
}

type FDGraphDisplayProps = {
    graph: FDGraphType;
};

export function FDGraphDisplay({ graph }: FDGraphDisplayProps) {
    const rfGraph = useMemo(() => createRFGraph(graph), [ graph ]);

    return (
        <div className='w-full h-[700px]'>
            <ReactFlow
                fitView
                nodes={rfGraph.nodes}
                edges={rfGraph.edges}
            />
        </div>
    );
}
