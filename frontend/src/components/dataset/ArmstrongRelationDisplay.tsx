import { type ExampleRelation, type ArmstrongRelation, type ExampleRow, ExampleState } from '@/types/armstrongRelation';
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react';
import { ColumnNameBadge } from './FDListDisplay';
import clsx, { type ClassValue } from 'clsx';
import { type Dispatch, type Key, useMemo, useState } from 'react';
import { BiCollapseHorizontal, BiExpandHorizontal } from 'react-icons/bi';
import { ExampleStateIcon } from '../WorkersDistribution';
import { IoCheckmark, IoClose, IoHelp } from 'react-icons/io5';
import { ColumnState, useDecisionContext } from '@/context/DecisionProvider';
import { type IconType } from 'react-icons';
import { type AssignmentInfo } from '@/types/assignment';
import { Link } from 'react-router';
import { routes } from '@/router';

type GridState = {
    isProgressCollapsed?: boolean;
    isMaxSetCollapsed?: boolean;
};

type ArmstrongRelationDisplayProps = {
    relation: ArmstrongRelation;
    workerOptions: WorkerOption[];
    assignWorker: (rowIndex: number, workerId?: string) => void;
    assignments?: AssignmentInfo[];
};

export function ArmstrongRelationDisplay({ relation, workerOptions, assignWorker, assignments }: ArmstrongRelationDisplayProps) {
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
                    onPress={() => setState(prev => ({ ...prev, isMaxSetCollapsed: !prev.isMaxSetCollapsed }))}
                >
                    {state.isMaxSetCollapsed ? (
                        <BiExpandHorizontal size={20} />
                    ) : (
                        <BiCollapseHorizontal size={20} />
                    )}
                </Button>

                {!state.isMaxSetCollapsed && (
                    <span className='font-semibold text-primary'>
                        Maximal set
                    </span>
                )}
            </div>


            <ReferenceRowDisplay row={relation.referenceRow} />

            {relation.exampleRows.map((row, rowIndex) => (
                <ExampleRowDisplay
                    key={rowIndex}
                    relation={relation}
                    row={row}
                    rowIndex={rowIndex}
                    workerOptions={workerOptions}
                    assignWorker={assignWorker}
                    assignments={assignments}
                    gridState={state}
                />
            ))}
        </div>
    );
}

function getCellClass(rowIndex: number, colIndex: number, isHighlighted?: boolean, ...rest: ClassValue[]) {
    const bgClass = rowIndex % 2 === 0 ? 'bg-content1' : 'bg-content2';
    return clsx(
        'p-2 leading-5 align-top max-w-xl break-words',
        bgClass,
        colIndex !== 0 && 'border-l border-foreground-500',
        isHighlighted && 'text-primary font-semibold',
        ...rest,
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
    relation: ArmstrongRelation;
    row: ExampleRow;
    rowIndex: number;
    workerOptions: WorkerOption[];
    assignWorker: (rowIndex: number, workerId?: string) => void;
    assignments?: AssignmentInfo[];
    gridState: GridState;
};

function ExampleRowDisplay({ relation, row, rowIndex, workerOptions, assignWorker, assignments, gridState }: ExampleRowDisplayProps) {
    const maxSetCols = row.maxSet.map(index => relation.columns[index]);
    const exampleBgClass = row.isPositive ? 'bg-danger-400' : 'bg-warning-400';
    const { leftClass, rightClass } = getSpecialCellClasses(rowIndex);

    const worker = workerOptions.find(w => w.key === row.workerId);
    const isEvaluationAllowed = row.isPositive === relation.isEvaluatingPositives;

    const assignment = useMemo(() => assignments?.find(a => a.rowIndex === rowIndex), [ assignments, rowIndex ]);

    return (<>
        <div className={leftClass}>
            {(isEvaluationAllowed || row.state !== ExampleState.New) && (
                <ExampleStateIcon state={row.state} size={20} />
            )}

            {isEvaluationAllowed && !gridState.isProgressCollapsed && (<>
                {/* {worker ? (
                    <span className='font-semibold'>{worker.label}</span>
                ) : (
                    <WorkerDropdown workerOptions={workerOptions} selectWorker={key => assignWorker(rowIndex, key as string)} />
                )} */}

                {assignment ? (
                    <Link to={routes.assignment.root.resolve({ assignmentId: assignment.id })}>
                        <Button size='sm' className='h-6' disableAnimation>
                            {row.state === ExampleState.New ? 'Evaluate' : 'Re-evaluate'}
                        </Button>
                    </Link>
                ) : (
                    <Button size='sm' className='h-6' disableAnimation onPress={() => assignWorker(rowIndex)}>
                        Evaluate
                    </Button>
                )}
            </>)}
        </div>

        {row.values.map((value, colIndex) => (
            <div key={colIndex} className={getCellClass(rowIndex, colIndex, row.maxSet.includes(colIndex))}>
                {value}
            </div>
        ))}

        <div title={maxSetCols.join(', ')} className={rightClass}>
            {gridState.isMaxSetCollapsed ? (
                <div className={clsx('size-4 rounded-full', exampleBgClass)} />
            ) : (
                <div className='max-w-80 flex flex-wrap gap-x-2 gap-y-1'>
                    {maxSetCols.map(col => (
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
    selectedColIndex: number | undefined;
    setSelectedColIndex?: Dispatch<number | undefined>;
};

export function ExampleRelationDisplay({ relation: { columns, referenceRow, exampleRow }, selectedColIndex, setSelectedColIndex }: ExampleRelationDisplayProps) {
    const { decision } = useDecisionContext();

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

            {exampleRow.values.map((value, colIndex) => {
                const state = decision.columns[colIndex].state;

                // If the selection function isn't provided, the cells aren't interactive. Also, if the state isn't defined, the cell is in the maximal set.
                if (!setSelectedColIndex || !state) {
                    return (
                        <div key={colIndex} className={getCellClass(0, colIndex, !state)}>
                            {value}
                        </div>
                    );
                }

                const { color, icon } = columnStateData[state];
                const isSelected = colIndex === selectedColIndex;

                return (
                    <button
                        key={colIndex}
                        className={getCellClass(0, colIndex, false, 'flex justify-between gap-2 hover:bg-primary-200 active:bg-primary-300', isSelected && 'text-secondary')}
                        onClick={() => setSelectedColIndex(isSelected ? undefined : colIndex)}
                    >
                        {value}
                        {icon({ size: 20, className: color })}
                    </button>
                );
            })}
        </div>
    );
}

const columnStateData: Record<ColumnState, {
    color: string;
    icon: IconType;
}> = {
    [ColumnState.Undecided]: { color: 'text-warning', icon: IoHelp },
    [ColumnState.Valid]: { color: 'text-success', icon: IoCheckmark },
    [ColumnState.Invalid]: { color: 'text-danger', icon: IoClose },
};
