import { useMemo } from 'react';
import { createRFGraph, type FDGraph as FDGraphType } from '@/types/FD';
import ReactFlow from 'reactflow';
import 'reactflow/dist/style.css';

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
