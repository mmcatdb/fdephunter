import { createRFGraph, type FDGraph as FDGraphType } from '@/types/FD';
import { useMemo } from 'react';
import ReactFlow from 'reactflow';
import 'reactflow/dist/style.css';

type FDGraphProps = {
    graph: FDGraphType;
};

export function FDGraph({ graph }: FDGraphProps) {
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
