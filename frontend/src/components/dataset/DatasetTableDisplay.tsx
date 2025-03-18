import { useMemo } from 'react';
import { Table, TableBody, TableCell, TableColumn, TableHeader, TableRow } from '@heroui/react';
import type { DatasetData } from '@/types/dataset';

type DatasetTableProps = {
    data: DatasetData;
};

export function DatasetTable({ data }: DatasetTableProps) {
    const items = useMemo(() => data.rows.map((row, index) => ({ row, index })), [ data ]);

    return (
        <Table isStriped aria-label='Table of functional dependencies with negative examples'>
            <TableHeader>
                {data.header.map((column, index) => (
                    <TableColumn key={index}>{column}</TableColumn>
                ))}
            </TableHeader>
            <TableBody items={items}>
                {({ row, index }) => (
                    <TableRow key={index}>
                        {row.map((column, colIndex) => (
                            <TableCell key={colIndex}>{column}</TableCell>
                        ))}
                    </TableRow>
                )}
            </TableBody>
        </Table>
    );
}
