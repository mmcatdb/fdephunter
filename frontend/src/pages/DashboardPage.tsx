import { API } from '@/utils/api';
import { routes } from '@/router';
import { Workflow } from '@/types/workflow';
import { Link, useNavigate } from 'react-router-dom';
import { rawAPI } from '@/utils/api/rawAPI';
import { Portal, portals } from '@/components/common/Portal';
import { Button } from '@nextui-org/react';

export default function DashboardPage() {
    const navigate = useNavigate();

    async function continueToWorkflow() {
        const response = await API.workflows.create({}, {});
        if (!response.status)
            return;

        navigate(routes.workflow.detail.resolve({ workflowId: Workflow.fromServer(response.data).id }));
    }

    return (<>
        <Portal to={portals.topbar} >
            <TopbarToolbar />
        </Portal>
        <div className='container'>
            <h1>A catching title!</h1>
            <p className='mt-12'>
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            </p>
            <p>
                Even more catching description. Wanna know more?
            </p>

            <div className='mt-12 flex justify-center'>
                <div className='md:w-1/2 lg:w-1/3'>
                    <Button
                        className='w-full'
                        onPress={continueToWorkflow}
                    >
                        Sure thing!
                    </Button>
                </div>
            </div>
        </div>
    </>);
}

function TopbarToolbar() {
    function resetDatabase() {
        void rawAPI.POST('/demo/initialize');
    }

    return (
        <div>
            <Link to={routes.root}>
                Home
            </Link>
            <Link to={routes.workflow.example} className='ml-4'>
                Workflow
            </Link>
            <Link to={routes.worker.example} className='ml-4'>
                Worker
            </Link>
            <Link to={routes.assignment.example} className='ml-4'>
                Assignment
            </Link>
            <Button onPress={resetDatabase} className='ml-4' color='primary' variant='bordered'>
                Reset DB
            </Button>
        </div>
    );
}
