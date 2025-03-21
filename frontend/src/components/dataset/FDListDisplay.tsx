import type { FDEdge } from '@/types/FD';
import { cn } from '@heroui/react';
import { FaArrowRight } from 'react-icons/fa';
import { Fragment } from 'react/jsx-runtime';

type FDListDisplayProps = {
    edges: FDEdge[];
};

export function FDListDisplay({ edges }: FDListDisplayProps) {
    return (
        <div>
            {edges.map(edge => (
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
            {/* <div className='col-span-1'>{edge.id}</div> */}

            <div className='col-span-5 flex items-center gap-x-2'>
                {edge.source.columns.map(name => (
                    <ColumnNameBadge key={name} name={name} />
                ))}
            </div>

            <div className='col-span-2 flex justify-center'><FaArrowRight size={20} /></div>

            <div className='col-span-5 flex items-center gap-x-2'>
                {edge.target.columns.map(name => (
                    <ColumnNameBadge key={name} name={name} />
                ))}
            </div>
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

export function FDListDisplayBalanced({ edges }: FDListDisplayProps) {
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
