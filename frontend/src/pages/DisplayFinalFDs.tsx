import { DatasetTableView } from '@/components/dataset/DatasetTableView';
import { FDListView } from '@/components/dataset/FDListView';
import { FDGraphView } from '@/components/dataset/FDGraphView';
import { useJobResult } from '@/hooks';
import { type Workflow } from '@/types/workflow';
import { Tab, Tabs } from '@nextui-org/react';

type DisplayFinalFDsProps = {
    workflow: Workflow;
};

export function DisplayFinalFDs({ workflow }: DisplayFinalFDsProps) {
    const jobResult = useJobResult(workflow.id);

    return (
        <Tabs defaultSelectedKey='overview'>
            <Tab key='table' title='Dataset' className='pt-4'>
                <DatasetTableView workflowId={workflow.id} />
            </Tab>
            <Tab key='list' title='Functional dependencies' className='pt-4'>
                <FDListView graph={jobResult?.fdGraph} />
            </Tab>
            <Tab key='graph' title='Graph view' className='pt-4'>
                <FDGraphView graph={jobResult?.fdGraph} />
            </Tab>
        </Tabs>
    );
}
