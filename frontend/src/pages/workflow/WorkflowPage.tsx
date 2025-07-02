import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
import { Outlet, type Params, useLoaderData } from 'react-router';
import { Sidebar } from '@/components/layout';
// import { API } from '@/utils/api';
import { mockAPI } from '@/utils/api/mockAPI';
import { type Lattice } from '@/types/armstrongRelation';
import { type DatasetData } from '@/types/dataset';
import { JobResult } from '@/types/job';
import { type FdSet } from '@/types/functionalDependency';

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
    lattices: Lattice[];
    dataset: DatasetData;
    fdSet: FdSet;
    jobResult: JobResult | undefined;
};

WorkflowPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const responses = await Promise.all([
        mockAPI.workflow.getWorkflow(workflowId),
        mockAPI.view.getLattices(workflowId),
        mockAPI.dataset.getDatasetData(workflowId),
        mockAPI.view.getFds(workflowId),
        mockAPI.workflow.getLastJobResult(workflowId),
    ]);
    if (!responses[0].status)
        throw new Error('Failed to load workflow');
    if (!responses[1].status)
        throw new Error('Failed to load lattices');
    if (!responses[2].status)
        throw new Error('Failed to load dataset data');
    if (!responses[3].status)
        throw new Error('Failed to load FD classes');

    const jobResult = responses[4].status ? JobResult.fromResponse(responses[4].data) : undefined;

    return {
        workflow: Workflow.fromResponse(responses[0].data),
        lattices: responses[1].data,
        dataset: responses[2].data,
        fdSet: responses[3].data,
        jobResult,
    };
};
