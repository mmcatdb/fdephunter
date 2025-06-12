import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
import { Outlet, type Params, useLoaderData } from 'react-router';
import { Sidebar } from '@/components/layout';
// import { API } from '@/utils/api';
import { mockAPI } from '@/utils/api/mockAPI';
import { type McLattice } from '@/types/armstrongRelation';
import { type DatasetData } from '@/types/dataset';
import { type MockFDClass } from '@/utils/mockData';
import { JobResult } from '@/types/job';

export function WorkflowPage() {
    const { workflow } = useLoaderData<WorkflowLoaded>();

    return (<>
        <Sidebar>
            <WorkflowProgressDisplay currentStep={workflow.state} />
        </Sidebar>

        <Outlet />
    </>);
}

export type WorkflowLoaded = {
    workflow: Workflow;
    lattices: McLattice[];
    dataset: DatasetData;
    fdClasses: MockFDClass[];
    jobResult: JobResult | undefined;
};

WorkflowPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const responses = await Promise.all([
        mockAPI.workflows.get(workflowId),
        mockAPI.assignments.getLattices(workflowId),
        mockAPI.workflows.getDatasetData(workflowId),
        mockAPI.workflows.getFdClasses(workflowId),
        mockAPI.workflows.getLastJobResult(workflowId),
    ]);
    if (!responses[0].status)
        throw new Error('Failed to load workflow');
    if (!responses[1].status)
        throw new Error('Failed to load lattices');
    if (!responses[2].status)
        throw new Error('Failed to load dataset data');
    if (!responses[3].status)
        throw new Error('Failed to load FD classes');

    const jobResult = responses[4].status ? JobResult.fromServer(responses[4].data) : undefined;

    return {
        workflow: Workflow.fromServer(responses[0].data),
        lattices: responses[1].data,
        dataset: responses[2].data,
        fdClasses: responses[3].data,
        jobResult,
    };
};
