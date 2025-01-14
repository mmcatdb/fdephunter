import { Tab, Tabs } from 'react-bootstrap';
import DatasetTableView from '@/components/dataset/DatasetTableView';
import FDListView from '@/components/dataset/FDListView';
import FDGraphView from '@/components/dataset/FDGraphView';
import { useJobResult } from '@/hooks';
import { type Workflow } from '@/types/workflow';

export type DisplayFinalFDsProps = {
    workflow: Workflow;
}

export default function DisplayFinalFDs({ workflow }: DisplayFinalFDsProps) {
    const jobResult = useJobResult(workflow.id);

    return (<>
        <Tabs
            defaultActiveKey='overview'
        >
            <Tab eventKey='table' title='Dataset' className='pt-3'>
                <DatasetTableView workflowId={workflow.id} />
            </Tab>
            <Tab eventKey='list' title='Functional dependencies' className='pt-3'>
                <FDListView graph={jobResult?.fdGraph} />
            </Tab>
            <Tab eventKey='graph' title='Graph view' className='pt-3'>
                <FDGraphView graph={jobResult?.fdGraph} />
            </Tab>
        </Tabs>
    </>);
}
