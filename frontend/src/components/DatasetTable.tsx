import { DecisionPhase, type DecisionState, useDecisionContext, useTryDecisionContext } from '@/context/DecisionProvider';
import type { DatasetRow, DatasetDataWithExamples, DatasetData } from '@/types/dataset';
import { Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@nextui-org/react';
import clsx from 'clsx';
import { useMemo } from 'react';

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
        <Table isStriped>
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
            <TableRow key={-rowIndex} className='fd-negative-example-row'>
                {row.map((column, colIndex) => (
                    <TableCell key={colIndex} className='fd-column'>
                        <div className='fd-column-inner'>
                            {column}
                        </div>
                    </TableCell>
                ))}
            </TableRow>
        );
    }

    return (
        <TableRow key={-rowIndex}>
            {row.map((column, colIndex) => (
                <NegativeExampleColumn key={colIndex} rowIndex={rowIndex} colIndex={colIndex} value={column} />
            ))}
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
                : {
                    colIndex: colIndex,
                    rowIndex,
                },
        });
    }

    return (
        <td className={clsx('fd-column', isNegative ? 'negative' : 'positive', isSelected && 'selected')}>
            {/* TODO Replace by button. */}
            <div className='fd-column-inner evaluated' onClick={columnClicked}>
                <span className='fd-column-value'>{value}</span>
                <div>
                    <span className='fd-column-reasons'>
                        <span>{isNegative ? 'Negative' : 'Positive'}</span>{' '}
                        {isNegative && <span>(<span className='font-bold'>{column.reasons.length}</span> {column.reasons.length === 1 ? 'reason' : 'reasons'})</span>}
                    </span>
                </div>
            </div>
        </td>
    );
}
