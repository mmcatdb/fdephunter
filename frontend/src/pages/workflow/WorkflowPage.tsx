import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
import { Outlet, type Params, useLoaderData } from 'react-router';
import { Sidebar } from '@/components/layout';
import { API } from '@/utils/api';

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
};

WorkflowPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const response = await API.workflows.get(undefined, { workflowId });
    if (!response.status)
        throw new Error('Failed to load workflow');

    return {
        workflow: Workflow.fromServer(response.data),
    };
};
