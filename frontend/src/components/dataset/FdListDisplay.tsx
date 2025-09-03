import type { FdEdge } from '@/types/functionalDependency';
import { cn } from '@heroui/react';
import { FaArrowRight } from 'react-icons/fa';
import { Fragment } from 'react/jsx-runtime';

type FdListDisplayProps = {
    edges: FdEdge[];
};

export function FdListDisplay({ edges }: FdListDisplayProps) {
    if (edges.length === 0) {
        return (
            <p className='text-center'>No functional dependencies found.</p>
        );
    }

    return (
        <div className='grid grid-cols-[min-content,80px,min-content] justify-center gap-2'>
            {edges.map(edge => (
                <Fragment key={edge.id}>
                    <div className='flex items-center gap-x-2'>
                        {edge.source.columns.map(name => (
                            <ColumnNameBadge key={name} name={name} />
                        ))}
                    </div>

                    <div className='flex justify-center'><FaArrowRight size={20} /></div>

                    <div className='flex items-center gap-x-2'>
                        {edge.target.columns.map(name => (
                            <ColumnNameBadge key={name} name={name} />
                        ))}
                    </div>
                </Fragment>
            ))}
        </div>
    );
}

type ColumnNameBadgeProps = {
    name: string;
    className?: string;
};

export function ColumnNameBadge({ name, className }: ColumnNameBadgeProps) {
    return (
        <div className={cn('inline-block w-fit h-4 px-2 rounded-full text-sm/4 font-semibold bg-primary text-white truncate', className)}>{name}</div>
    );
}
