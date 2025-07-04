import { type Dispatch, useState } from 'react';
import { type ExampleRelation, DecisionColumnStatus, type ExampleDecision, DecisionStatus } from '@/types/armstrongRelation';
import { Button } from '@heroui/react';
import { ColumnNameBadge } from './FdListDisplay';
import clsx, { type ClassValue } from 'clsx';
import { BiCollapseHorizontal, BiExpandHorizontal } from 'react-icons/bi';
import { IoCheckmark, IoClose, IoHelp, IoCheckmarkCircleOutline, IoCloseCircleOutline, IoReloadCircleOutline } from 'react-icons/io5';
import { useDecisionContext } from '@/context/DecisionProvider';
import { type IconType } from 'react-icons';
import { type Assignment } from '@/types/assignment';
import { Link } from 'react-router';
import { routes } from '@/router';

type GridState = {
    isProgressCollapsed?: boolean;
    isMaxSetCollapsed?: boolean;
};

type ArmstrongRelationDisplayProps = {
    assignments: Assignment[];
};

export function ArmstrongRelationDisplay({ assignments }: ArmstrongRelationDisplayProps) {
    const [ state, setState ] = useState<GridState>({});

    const columns = assignments[0].relation.columns;

    return (
        <div
            className='grid overflow-x-auto pb-4 leading-5'
            style={{ gridTemplateColumns: `repeat(${columns.length + 2}, max-content)` }}
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

            {columns.map((col, colIndex) => (
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
                        Max set
                    </span>
                )}
            </div>


            <ReferenceRowDisplay rowValues={assignments[0].relation.referenceRow} />

            {assignments.map((assignment, rowIndex) => (
                <ExampleRowDisplay
                    key={assignment.id}
                    assignment={assignment}
                    gridState={state}
                    rowIndex={rowIndex}
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
    rowValues: string[];
};

function ReferenceRowDisplay({ rowValues }: ReferenceRowDisplayProps) {
    const { leftClass, rightClass } = getSpecialCellClasses(-1);

    return (<>
        <div className={leftClass} />

        {rowValues.map((value, colIndex) => (
            <div key={colIndex} className={getCellClass(-1, colIndex, true)}>
                {value}
            </div>
        ))}

        <div className={rightClass} />
    </>);
}

type ExampleRowDisplayProps = {
    assignment: Assignment;
    gridState: GridState;
    rowIndex: number;
};

function ExampleRowDisplay({ assignment, gridState, rowIndex }: ExampleRowDisplayProps) {
    const row = assignment.relation.exampleRow;

    const lhsSetCols = row.lhsSet.map(assignment.relation.columns);
    const exampleBgClass = row.isPositive ? 'bg-danger-400' : 'bg-warning-400';
    const { leftClass, rightClass } = getSpecialCellClasses(rowIndex);

    // TODO Get this information somewhere.
    // const isEvaluationAllowed = row.isPositive === relation.isEvaluatingPositives;
    const isEvaluationAllowed = true;

    return (<>
        <div className={leftClass}>
            {(isEvaluationAllowed || row.decision) && (
                <ExampleDecisionIcon decision={row.decision} size={20} />
            )}

            {isEvaluationAllowed && !gridState.isProgressCollapsed && (<>
                {assignment ? (
                    <Link to={routes.assignment.root.resolve({ assignmentId: assignment.id })}>
                        <Button size='sm' className='h-6' disableAnimation>
                            {!row.decision ? 'Evaluate' : 'Re-evaluate'}
                        </Button>
                    </Link>
                ) : (
                    <div>
                        {/* TODO */}
                        TODO GENERATE THE ASSIGNMENTS
                    </div>
                )}
            </>)}
        </div>

        {row.values.map((value, colIndex) => (
            <div key={colIndex} className={getCellClass(rowIndex, colIndex, row.lhsSet.has(colIndex))}>
                {value}
            </div>
        ))}

        <div title={lhsSetCols.join(', ')} className={rightClass}>
            {gridState.isMaxSetCollapsed ? (
                <div className={clsx('size-4 rounded-full', exampleBgClass)} />
            ) : (
                <div className='max-w-80 flex flex-wrap gap-x-2 gap-y-1'>
                    {lhsSetCols.map(col => (
                        <ColumnNameBadge key={col} name={col} className={exampleBgClass} />
                    ))}
                </div>
            )}
        </div>
    </>);
}

type ExampleDecisionIconProps = {
    decision: ExampleDecision | undefined;
    size?: number;
};

function ExampleDecisionIcon({ decision, size = 24 }: ExampleDecisionIconProps) {
    const data = decision ? decisionStatusData[decision.status] : { color: 'text-primary', icon: IoReloadCircleOutline };

    return (
        <data.icon size={size} className={data.color} />
    );
}

const decisionStatusData: Record<DecisionStatus, {
    color: string;
    icon: IconType;
}> = {
    [DecisionStatus.Accepted]: { color: 'text-success', icon: IoCheckmarkCircleOutline },
    [DecisionStatus.Rejected]: { color: 'text-danger', icon: IoCloseCircleOutline },
    [DecisionStatus.Unanswered]: { color: 'text-warning', icon: IoReloadCircleOutline },
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

            {referenceRow.map((value, colIndex) => (
                <div key={colIndex} className={getCellClass(-1, colIndex, true)}>
                    {value}
                </div>
            ))}

            {exampleRow.values.map((value, colIndex) => {
                const status = decision.columns[colIndex].status;

                // If the selection function isn't provided, the cells aren't interactive. Also, if the status isn't defined, the cell isn't in the rhs set so it can't be selected either.
                if (!setSelectedColIndex || !status) {
                    return (
                        <div key={colIndex} className={getCellClass(0, colIndex, !status)}>
                            {value}
                        </div>
                    );
                }

                const { color, icon } = columnStateData[status];
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

const columnStateData: Record<DecisionColumnStatus, {
    color: string;
    icon: IconType;
}> = {
    [DecisionColumnStatus.Undecided]: { color: 'text-warning', icon: IoHelp },
    [DecisionColumnStatus.Valid]: { color: 'text-success', icon: IoCheckmark },
    [DecisionColumnStatus.Invalid]: { color: 'text-danger', icon: IoClose },
};
