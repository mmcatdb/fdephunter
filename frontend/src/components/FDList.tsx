import type { FDEdge, FDGraph } from '@/types/FD';
import { Badge } from '@nextui-org/react';
import { FaArrowRight } from 'react-icons/fa';

type FDListProps = {
    graph: FDGraph;
};

export function FDList({ graph }: FDListProps) {
    return (
        <div>
            {graph.edges.map(edge => (
                <FDRow key={edge.id} edge={edge} />
            ))}
        </div>
    );
}

type FDRowProps = {
    edge: FDEdge;
};

function FDRow({ edge }: FDRowProps) {
    return (
        <div className='py-1 grid grid-cols-12 gap-4'>
            <div className='col-span-1'>{edge.id}</div>
            <div className='col-span-6'>
                {edge.source.columns.map(name => <ColumnNameBadge key={name} name={name} />)}
            </div>
            <div className='col-span-2'><FaArrowRight size={20} /></div>
            <div className='col-span-3'>
                {edge.target.columns.map(name => <ColumnNameBadge key={name} name={name} />)}
            </div>
        </div>
    );
}

type ColumnNameBadgeProps = {
    name: string;
};

function ColumnNameBadge({ name }: ColumnNameBadgeProps) {
    return (
        <Badge className='fd-column-name-badge'>{name}</Badge>
    );
}
