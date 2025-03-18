// import { API } from '@/utils/api';
import { routes } from '@/router';
import { Workflow } from '@/types/workflow';
import { Link, useNavigate } from 'react-router';
// import { rawAPI } from '@/utils/api/rawAPI';
import { Button } from '@heroui/react';
import { Page, TopbarContent } from '@/components/layout';
import { mockAPI } from '@/utils/api/mockAPI';
import { useState } from 'react';

export function LandingPage() {
    const [ isFetching, setIsFetching ] = useState(false);
    const navigate = useNavigate();

    async function continueToWorkflow() {
        setIsFetching(true);
        // const response = await API.workflows.create({}, {});
        const response = await mockAPI.workflows.create();
        if (!response.status) {
            setIsFetching(false);
            return;
        }

        void navigate(routes.workflow.settings.resolve({ workflowId: Workflow.fromServer(response.data).id }));
    }

    return (
        <Page>
            <TopbarContent>
                <TopbarToolbar />
            </TopbarContent>

            <h1 className='text-lg'>FDepHunter</h1>

            <p className='mt-12'>
                Functional dependencies (FDs) are key to understanding your data, but standard discovery methods can miss important insights. FDepHunter goes beyond traditional approaches by eliminating false positives (coincidental FDs) and revealing hidden patterns (ghost FDs) that standard techniques overlook.
                <br /><br />

                By enhancing datasets with intelligently generated examples, FDepHunter improves accuracy and provides a clearer picture of real-world data relationships. Whether {'you\'re'} profiling data for research, optimization, or compliance, FDepHunter helps you make more informed decisions.
                <br /><br />

                Try FDepHunter today and uncover the true structure of your data!
            </p>

            <div className='mt-12 flex flex-col items-center gap-4'>
                <Button color='primary' className='md:w-1/2 lg:w-1/3' onPress={continueToWorkflow} isLoading={isFetching}>
                    Sure thing!
                </Button>

                <Button color='secondary' className='md:w-1/2 lg:w-1/3'>
                    Read documentation
                </Button>
            </div>
        </Page>
    );
}

function TopbarToolbar() {
    // function resetDatabase() {
    //     void rawAPI.POST('/demo/initialize');
    // }

    return (
        <div className='space-x-4'>
            <Link to={routes.root} className='hover:underline'>
                Home
            </Link>
            <Link to={routes.workflow.example} className='hover:underline'>
                Workflow
            </Link>
            <Link to={routes.worker.example} className='hover:underline'>
                Worker
            </Link>
            <Link to={routes.assignment.example} className='hover:underline'>
                Assignment
            </Link>
            {/* <Button color='secondary' size='sm' onPress={resetDatabase}>
                Reset DB
            </Button> */}
        </div>
    );
}
