import { API } from '@/utils/api';
import { routes } from '@/router';
import { Workflow } from '@/types/workflow';
import { Link, useNavigate } from 'react-router-dom';
import { rawAPI } from '@/utils/api/rawAPI';
import { Button } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';

export function LandingPage() {
    const navigate = useNavigate();

    async function continueToWorkflow() {
        const response = await API.workflows.create({}, {});
        if (!response.status)
            return;

        navigate(routes.workflow.detail.resolve({ workflowId: Workflow.fromServer(response.data).id }));
    }

    return (
        <Page>
            <TopbarContent>
                <TopbarToolbar />
            </TopbarContent>

            <h1 className='text-lg'>A catching title!</h1>
            <p className='mt-10'>
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            </p>
            <p>
                Even more catching description. Wanna know more?
            </p>

            <div className='mt-10 flex justify-center'>
                <div className='md:w-1/2 lg:w-1/3'>
                    <Button color='primary' className='w-full' onPress={continueToWorkflow}>
                        Sure thing!
                    </Button>
                </div>
            </div>
        </Page>
    );
}

function TopbarToolbar() {
    function resetDatabase() {
        void rawAPI.POST('/demo/initialize');
    }

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
            <Button color='secondary' size='sm' onPress={resetDatabase}>
                Reset DB
            </Button>
        </div>
    );
}
