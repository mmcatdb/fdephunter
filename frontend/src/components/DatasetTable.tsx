import useDecisionContext, { DecisionPhase } from '@/context/DecisionProvider';
import type { DatasetRow, DatasetDataWithExamples, DatasetData } from '@/types/dataset';
import clsx from 'clsx';
import { Table } from 'react-bootstrap';

type DatasetTableProps = {
    data: DatasetData | DatasetDataWithExamples;
};

export default function DatasetTable({ data }: DatasetTableProps) {
    return (
        <Table striped hover>
            <thead>
                <tr>
                    {data.header.map((column, index) => (
                        <th key={index}>{column}</th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {data.rows.map((row, index) => (
                    <DatasetTableRow key={index} row={row} />
                ))}
                {'examples' in data && data.examples.map((row, index) => (
                    <NegativeExampleRow key={index} rowIndex={index} value={row} />
                ))}
            </tbody>
        </Table>
    );
}

type DatasetTableRowProps = {
    row: DatasetRow;
};

function DatasetTableRow({ row }: DatasetTableRowProps) {
    return (
        <tr>
            {row.map((column, index) => (
                <td key={index}>{column}</td>
            ))}
        </tr>
    );
}

type NegativeExampleRowProps = {
    rowIndex: number;
    value: DatasetRow;
};

function NegativeExampleRow({ rowIndex, value }: NegativeExampleRowProps) {
    const { decision } = useDecisionContext();

    if (decision.phase === DecisionPhase.AnswerYesNo) {
        return (
            <tr className='fd-negative-example-row'>
                {value.map((column, colIndex) => (
                    <td key={colIndex} className='fd-column'>
                        <div className='fd-column-inner'>
                            {column}
                        </div>
                    </td>
                ))}
            </tr>
        );
    }

    return (
        <tr>
            {value.map((column, colIndex) => (
                <NegativeExampleColumn key={colIndex} rowIndex={rowIndex} colIndex={colIndex} value={column} />
            ))}
        </tr>
    );
}

type NegativeExampleColumnProps = {
    rowIndex: number;
    colIndex: number;
    value: string;
}

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
            <div className='fd-column-inner evaluated' onClick={columnClicked}>
                <span className='fd-column-value'>{value}</span>
                <div>
                    <span className='fd-column-reasons'>
                        <span>{isNegative ? 'Negative' : 'Positive'}</span>{' '}
                        {isNegative && <span>(<span className='fw-bold'>{column.reasons.length}</span> {column.reasons.length === 1 ? 'reason' : 'reasons'})</span>}
                    </span>
                </div>
            </div>
        </td>
    );
}