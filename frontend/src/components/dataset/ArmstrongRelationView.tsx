import { type ArmstrongRelation } from '@/types/armstrongRelation';
import { Card } from '@nextui-org/react';
import { ColumnNameBadge } from './FDListView';

type ArmstrongRelationViewProps = {
    relation?: ArmstrongRelation;
};

export function ArmstrongRelationView({ relation }: ArmstrongRelationViewProps) {
    if (!relation)
        // return null;
        relation = TEST_RELATION;

    return (
        <div className='flex flex-col items-start'>
            <Card className='p-4'>
                <div className='overflow-x-auto pb-4'>
                    <ArmstrongRelationTable relation={relation} />
                </div>
            </Card>
        </div>
    );
}

type ArmstrongRelationTableProps = {
    relation: ArmstrongRelation;
};

export function ArmstrongRelationTable({ relation }: ArmstrongRelationTableProps) {
    return (
        <table className='[&_td]:p-2 [&_td]:leading-5 [&_td]:align-top [&_td]:max-w-xl [&_td]:break-words'>
            <thead>
                <tr className='font-semibold'>
                    {relation.columns.map(col => (
                        <th key={col} title={col} className='p-2 max-w-xl'>
                            <div className='max-w-xl flex overflow-hidden'>
                                <ColumnNameBadge name={col} />
                            </div>
                        </th>
                    ))}
                    <th className='text-primary'>
                        Maximal set
                    </th>
                </tr>
            </thead>

            <tbody>
                {relation.rows.map((row, rowIndex) => (
                    <tr key={rowIndex} className='odd:bg-content3'>
                        {row.values.map((value, colIndex) => (
                            <td key={colIndex}>
                                {value}
                            </td>
                        ))}

                        <td title={row.maximalSet?.map(index => relation.columns[index]).join(', ')}>
                            {row.maximalSet && (
                                <div className='max-w-80 py-[2px] flex flex-wrap gap-1'>
                                    {row.maximalSet?.map(index => (
                                        <ColumnNameBadge key={relation.columns[index]} name={relation.columns[index]} />
                                    ))}
                                </div>
                            )}
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

const TEST_RELATION: ArmstrongRelation = {
    columns: [ 'tconst', 'primaryTitle', 'startYear', 'runtimeMinutes', 'genres' ],
    rows: [
        { maximalSet: undefined, isNegative: undefined, values: [ 'tt0036443', 'Titanic', '1943', '85', 'Action+Drama+History' ] },
        { maximalSet: [ 1,2 ], isNegative: false, values: [ 'tt0079836', 'Titanic', '1943', '194', 'Drama+History' ] },
        { maximalSet: [ 1,3 ], isNegative: false, values: [ 'tt0115392', 'Titanic', '1979', '85', 'Drama+Romance' ] },
        { maximalSet: [ 1,4 ], isNegative: false, values: [ 'tt0120338', 'Titanic', '1996', '87', 'Action+Drama+History' ] },
        { maximalSet: [ 3 ], isNegative: false, values: [ 'tt0143942', 'S.O.S. Titanic', '1997', '85', 'History' ] },
    ],
};
