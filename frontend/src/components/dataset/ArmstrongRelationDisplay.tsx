import { type ExampleRelation, type ArmstrongRelation, type ExampleRow } from '@/types/armstrongRelation';
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@nextui-org/react';
import { ColumnNameBadge } from './FDListDisplay';
import clsx from 'clsx';
import { type Key, useState } from 'react';
import { BiCollapseHorizontal, BiExpandHorizontal } from 'react-icons/bi';
import { ExampleStateIcon } from '../WorkersDistribution';

type GridState = {
    isProgressCollapsed?: boolean;
    isMaximalSetCollapsed?: boolean;
};

type ArmstrongRelationDisplayProps = {
    relation: ArmstrongRelation;
    workerOptions: WorkerOption[];
    assignWorker: (rowIndex: number, workerId: string) => void;
};

export function ArmstrongRelationDisplay({ relation, workerOptions, assignWorker }: ArmstrongRelationDisplayProps) {
    const [ state, setState ] = useState<GridState>({});

    return (
        <div
            className='grid overflow-x-auto pb-4 leading-5'
            style={{ gridTemplateColumns: `repeat(${relation.columns.length + 2}, max-content)` }}
        >
            <div className='sticky left-0 p-1 flex items-center gap-2 bg-content1 border-r border-foreground-500'>
                <Button
                    isIconOnly
                    className='!w-7 !h-6 !min-w-6'
                    size='sm'
                    variant='light'
                    color='primary'
                    onPress={() => setState(prev => ({ ...prev, isProgressCollapsed: !prev.isProgressCollapsed }))}
                >
                    {state.isProgressCollapsed ? (
                        <BiExpandHorizontal size={20} />
                    ) : (
                        <BiCollapseHorizontal size={20} />
                    )}
                </Button>

                {!state.isProgressCollapsed && (
                    <span className='font-semibold text-primary'>
                        Progress
                    </span>
                )}
            </div>

            {relation.columns.map((col, colIndex) => (
                <div key={col} title={col} className={clsx('p-2 max-w-xl flex items-center overflow-hidden', colIndex !== 0 && 'border-l border-foreground-500')}>
                    <ColumnNameBadge name={col} />
                </div>
            ))}

            <div className='sticky right-0 p-1 flex items-center gap-2 bg-content1 border-l border-foreground-500'>
                <Button
                    isIconOnly
                    className='!w-7 !h-6 !min-w-6'
                    size='sm'
                    variant='light'
                    color='primary'
                    onPress={() => setState(prev => ({ ...prev, isMaximalSetCollapsed: !prev.isMaximalSetCollapsed }))}
                >
                    {state.isMaximalSetCollapsed ? (
                        <BiExpandHorizontal size={20} />
                    ) : (
                        <BiCollapseHorizontal size={20} />
                    )}
                </Button>

                {!state.isMaximalSetCollapsed && (
                    <span className='font-semibold text-primary'>
                        Maximal set
                    </span>
                )}
            </div>


            <ReferenceRowDisplay row={relation.referenceRow} />

            {relation.exampleRows.map((row, rowIndex) => (
                <ExampleRowDisplay
                    key={rowIndex}
                    columns={relation.columns}
                    row={row}
                    rowIndex={rowIndex}
                    workerOptions={workerOptions}
                    assignWorker={assignWorker}
                    gridState={state}
                />
            ))}
        </div>
    );
}
function getCellClass(rowIndex: number, colIndex: number, isHighlighted?: boolean) {
    const bgClass = rowIndex % 2 === 0 ? 'bg-content1' : 'bg-content2';
    return clsx(
        'p-2 leading-5 align-top max-w-xl break-words',
        bgClass,
        colIndex !== 0 && 'border-l border-foreground-500',
        isHighlighted && 'text-primary font-semibold',
    );
}

function getSpecialCellClasses(rowIndex: number) {
    const bgClass = rowIndex % 2 === 0 ? 'bg-primary-50' : 'bg-primary-100';

    return {
        leftClass: clsx('sticky left-0 px-2 flex items-center gap-2 border-r border-foreground-500', bgClass),
        rightClass: clsx('sticky right-0 p-[10px] border-l border-foreground-500', bgClass),
    };
}

type ReferenceRowDisplayProps = {
    row: { values: string[] };
};

function ReferenceRowDisplay({ row }: ReferenceRowDisplayProps) {
    const { leftClass, rightClass } = getSpecialCellClasses(-1);

    return (<>
        <div className={leftClass} />

        {row.values.map((value, colIndex) => (
            <div key={colIndex} className={getCellClass(-1, colIndex, true)}>
                {value}
            </div>
        ))}

        <div className={rightClass} />
    </>);
}

type ExampleRowDisplayProps = {
    columns: string[];
    row: ExampleRow;
    rowIndex: number;
    workerOptions: WorkerOption[];
    assignWorker: (rowIndex: number, workerId: string) => void;
    gridState: GridState;
};

function ExampleRowDisplay({ columns, row, rowIndex, workerOptions, assignWorker, gridState }: ExampleRowDisplayProps) {
    const maximalSetCols = row.maximalSet.map(index => columns[index]);
    const exampleBgClass = row.isNegative ? 'bg-warning-400' : 'bg-danger-400';
    const { leftClass, rightClass } = getSpecialCellClasses(rowIndex);

    const worker = workerOptions.find(w => w.key === row.workerId);

    return (<>
        <div className={leftClass}>
            <ExampleStateIcon state={row.state} size={20} />

            {!gridState.isProgressCollapsed && (
                worker ? (
                    <span className='font-semibold'>{worker.label}</span>
                ) : (
                    <WorkerDropdown workerOptions={workerOptions} selectWorker={key => assignWorker(rowIndex, key as string)} />
                )
            )}
        </div>

        {row.values.map((value, colIndex) => (
            <div key={colIndex} className={getCellClass(rowIndex, colIndex, row.maximalSet.includes(colIndex))}>
                {value}
            </div>
        ))}

        <div title={maximalSetCols.join(', ')} className={rightClass}>
            {gridState.isMaximalSetCollapsed ? (
                <div className={clsx('size-4 rounded-full', exampleBgClass)} />
            ) : (
                <div className='max-w-80 flex flex-wrap gap-x-2 gap-y-1'>
                    {maximalSetCols.map(col => (
                        <ColumnNameBadge key={col} name={col} className={exampleBgClass} />
                    ))}
                </div>
            )}
        </div>
    </>);
}

type WorkerDropdownProps = {
    workerOptions: WorkerOption[];
    selectWorker: (key: Key) => void;
};

function WorkerDropdown({ workerOptions, selectWorker }: WorkerDropdownProps) {
    return (
        <Dropdown>
            <DropdownTrigger>
                <Button size='sm' className='h-6' disableAnimation>
                    Assign
                </Button>
            </DropdownTrigger>
            <DropdownMenu aria-label='Assign domain expert' items={workerOptions} onAction={selectWorker}>
                {item => (
                    <DropdownItem
                        key={item.key}
                        className={item.key === 'delete' ? 'text-danger' : ''}
                        color={item.key === 'delete' ? 'danger' : 'default'}
                    >
                        {item.label}
                    </DropdownItem>
                )}
            </DropdownMenu>
        </Dropdown>
    );
}

export type WorkerOption = {
    key: string;
    label: string;
};

type ExampleRelationDisplayProps = {
    relation: ExampleRelation;
};

export function ExampleRelationDisplay({ relation: { columns, referenceRow, exampleRow } }: ExampleRelationDisplayProps) {
    const maximalSetCols = exampleRow.maximalSet.map(index => columns[index]);
    const exampleBgClass = exampleRow.isNegative ? 'bg-warning-400' : 'bg-danger-400';

    return (
        <div
            className='grid overflow-x-auto pb-4 leading-5'
            style={{ gridTemplateColumns: `repeat(${columns.length}, max-content)` }}
        >
            {columns.map((col, colIndex) => (
                <div key={col} title={col} className={clsx('p-2 max-w-xl flex items-center overflow-hidden', colIndex !== 0 && 'border-l border-foreground-500')}>
                    <ColumnNameBadge name={col} />
                </div>
            ))}

            {referenceRow.values.map((value, colIndex) => (
                <div key={colIndex} className={getCellClass(-1, colIndex, true)}>
                    {value}
                </div>
            ))}

            {exampleRow.values.map((value, colIndex) => (
                <div key={colIndex} className={getCellClass(0, colIndex, exampleRow.maximalSet.includes(colIndex))}>
                    {value}
                </div>
            ))}

            <div className='col-span-full'>
                <div title={maximalSetCols.join(', ')} className='max-w-80 px-2 py-[10px] flex flex-wrap gap-x-2 gap-y-1'>
                    {maximalSetCols.map(col => (
                        <ColumnNameBadge key={col} name={col} className={exampleBgClass} />
                    ))}
                </div>
            </div>
        </div>
    );
}
