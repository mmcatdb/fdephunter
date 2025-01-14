import { DatasetTable } from '@/components/DatasetTable';
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
