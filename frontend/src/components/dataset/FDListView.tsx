import { FDList } from '@/components/FDList';
import { type FDGraph } from '@/types/FD';

type FDListViewProps = {
    graph?: FDGraph;
};

export function FDListView({ graph }: FDListViewProps) {
    if (!graph)
        return null;

    return (
        <FDList graph={graph} />
    );
}
