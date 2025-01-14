import { FDGraph } from '@/components/FDGraph';
import { type FDGraph as FdGraphType } from '@/types/FD';

type FDGraphViewProps = {
    graph?: FdGraphType;
};

export function FDGraphView({ graph }: FDGraphViewProps) {
    if (!graph)
        return null;

    return (
        <FDGraph graph={graph} />
    );
}
