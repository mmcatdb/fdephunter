import { JobDisplay } from '@/components/job/JobDisplay';
import { Page } from '@/components/layout';
import { routes } from '@/router';
import { Job, JobState } from '@/types/job';
import { Button } from '@heroui/react';
import { type Params, useLoaderData, useNavigate, useRevalidator, useRouteLoaderData } from 'react-router';
import { type WorkflowLoaded } from './WorkflowPage';
// import { API } from '@/utils/api';
import { useEffect } from 'react';
import { mockAPI } from '@/utils/api/mockAPI';
import { WorkflowState } from '@/types/workflow';

const REFRESH_TIMEOUT = 2000; // in ms

export function WorkflowJobPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const job = useLoaderData<Job>();

    const revalidator = useRevalidator();

    useEffect(() => {
        const interval = setInterval(() => {
            void revalidator.revalidate();
        }, REFRESH_TIMEOUT);

        return () => clearInterval(interval);
    }, [ revalidator ]);

    const navigate = useNavigate();

    function handleNextStep() {
        const route = workflow.state === WorkflowState.DisplayFinalFds
            ? routes.workflow.results.root
            : routes.workflow.dashboard.root;
        void navigate(route.resolve({ workflowId: workflow.id }));
    }

    return (
        <Page className='max-w-xl space-y-10'>
            <h1 className='text-lg'>
                {job.description}
            </h1>

            <JobDisplay job={job} />

            <div className='flex justify-end'>
                <Button color='primary' onPress={handleNextStep} isDisabled={job.state !== JobState.Finished}>
                    Continue
                </Button>
            </div>
        </Page>
    );
}

WorkflowJobPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<Job> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    // const response = await API.workflows.getLastJob(undefined, { workflowId });
    const response = await mockAPI.workflow.getLastJob(workflowId);
    if (!response.status)
        throw new Error('Failed to load job');

    return Job.fromResponse(response.data);
};
