import { useMemo } from 'react';
import { Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import { DecisionPhase, type DecisionState, useDecisionContext, useTryDecisionContext } from '@/context/DecisionProvider';
import type { DatasetRow, DatasetDataWithExamples, DatasetData } from '@/types/dataset';
import clsx from 'clsx';
import { useWorkflowData } from '@/hooks';

type DatasetTableViewProps = {
    workflowId: string;
};

export function DatasetTableView({ workflowId }: DatasetTableViewProps) {
    const data = useWorkflowData(workflowId);

    if (!data)
        return null;

    return (
        <DatasetTable data={data} />
    );
}


type DatasetTableProps = {
    data: DatasetData | DatasetDataWithExamples;
};

export function DatasetTable({ data }: DatasetTableProps) {
    const items = useMemo(() => [
        ...data.rows.map((row, index) => ({ row, index, isNegative: false })),
        ...('examples' in data ? data.examples : []).map((row, index) => ({ row, index, isNegative: true })),
    ], [ data ]);

    const decision = useTryDecisionContext()?.decision;

    return (
        <Table isStriped aria-label='Table of functional dependencies with negative examples'>
            <TableHeader>
                {data.header.map((column, index) => (
                    <TableColumn key={index}>{column}</TableColumn>
                ))}
            </TableHeader>
            <TableBody items={items}>
                {({ row, index, isNegative }) => (
                    isNegative
                        ? negativeExampleRow(row, index, decision)
                        : datasetTableRow(row, index)
                )}
            </TableBody>
        </Table>
    );
}

function datasetTableRow(row: DatasetRow, rowIndex: number) {
    return (
        <TableRow key={rowIndex}>
            {row.map((column, colIndex) => (
                <TableCell key={colIndex}>{column}</TableCell>
            ))}
        </TableRow>
    );
}

function negativeExampleRow(row: DatasetRow, rowIndex: number, decision: DecisionState | undefined) {
    if (!decision)
        throw new Error('Decision context is not available.');

    if (decision.phase === DecisionPhase.AnswerYesNo) {
        return (
            <TableRow key={`${rowIndex}-ne`} className='text-primary'>
                {row.map((column, colIndex) => (
                    <TableCell key={colIndex} className='p-0'>
                        <div className='min-w-32 h-full px-3 py-2'>
                            {column}
                        </div>
                    </TableCell>
                ))}
            </TableRow>
        );
    }

    const selectedColumn = decision.selectedColumn;

    return (
        <TableRow key={`${rowIndex}-ne`}>
            {row.map((column, colIndex) => {
                const decisionColumn = decision.rows[rowIndex].columns[colIndex];
                const isNegative = decisionColumn.reasons.length !== 0;
                const isSelected = selectedColumn && selectedColumn.rowIndex === rowIndex && selectedColumn.colIndex === colIndex;

                return (
                    <TableCell key={colIndex} className={clsx('p-0', isNegative ? 'negative' : 'positive', isSelected && 'selected')}>
                        <NegativeExampleColumn rowIndex={rowIndex} colIndex={colIndex} value={column} />
                    </TableCell>
                );
            })}
        </TableRow>
    );
}

type NegativeExampleColumnProps = {
    rowIndex: number;
    colIndex: number;
    value: string;
};

function NegativeExampleColumn({ rowIndex, colIndex, value }: NegativeExampleColumnProps) {
    const { decision, setDecision } = useDecisionContext();
    const column = decision.rows[rowIndex].columns[colIndex];
    const isNegative = column.reasons.length !== 0;
    const isSelected = decision.selectedColumn && decision.selectedColumn.rowIndex === rowIndex && decision.selectedColumn.colIndex === colIndex;

    function columnClicked() {
        setDecision({
            ...decision,
            selectedColumn: isSelected
                ? undefined
                : { colIndex, rowIndex },
        });
    }

    return (
        // TODO Replace by button.
        <div className={clsx('min-w-32 h-full px-3 py-2 cursor-pointer rounded fd-column-inner hover:bg-primary-200 active:bg-primary-100', isSelected && 'bg-primary-200')} onClick={columnClicked}>
            <span className={isNegative ? 'text-danger' : 'text-success'}>{value}</span>

            <div className='min-w-32'>
                <span>{isNegative ? 'Negative' : 'Positive'}</span>{' '}
                {isNegative && (<>
                    <span className='font-bold'>{column.reasons.length}</span>
                    {column.reasons.length === 1 ? 'reason' : 'reasons'}
                </>)}
            </div>
        </div>
    );
}
