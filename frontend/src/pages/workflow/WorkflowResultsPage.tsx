import { Button, Card, CardBody, CardHeader, Tab, Tabs } from '@nextui-org/react';
import { type WorkflowLoaded } from './WorkflowPage';
import { Link, matchPath, Outlet, useLocation, useNavigate, useRouteLoaderData } from 'react-router';
import { routes } from '@/router';
import { Page, TopbarContent } from '@/components/layout';
import { useState } from 'react';
import { mockAPI } from '@/utils/api/mockAPI';
import { Workflow } from '@/types/workflow';

export function WorkflowResultsPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    return (<>
        <TopbarContent>
            <WorkflowResultsTabs workflowId={workflow.id} />
        </TopbarContent>

        <Page>
            <Outlet />
        </Page>
    </>);
}

function WorkflowResultsTabs({ workflowId }: { workflowId: string }) {
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.workflow.results.tabs.path, pathname)?.params.tab ?? 'index';

    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='index' title='Final results' {...{ as: Link, to: routes.workflow.results.root.resolve({ workflowId }) }} />
            <Tab key='dataset' title='Dataset' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'dataset' }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'list' }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'graph' }) }} />
        </Tabs>
    );
}

export function WorkflowFinalPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    const [ isFetching, setIsFetching ] = useState(false);
    const navigate = useNavigate();

    async function continueToWorkflow() {
        setIsFetching(true);
        const response = await mockAPI.workflows.create();
        if (!response.status) {
            setIsFetching(false);
            return;
        }

        void navigate(routes.workflow.settings.resolve({ workflowId: Workflow.fromServer(response.data).id }));
    }

    return (
        <div className='mx-auto w-fit flex flex-col gap-8'>
            <Card className='w-full'>
                <CardHeader>
                    <h1 className='text-lg'>Final results</h1>
                </CardHeader>

                <CardBody className='grid grid-cols-3 gap-x-8 gap-y-2'>
                    <div>Iteration:<span className='px-2 text-primary font-semibold'>{workflow.iteration}</span></div>

                    <div className='col-span-2 flex items-center'>Dataset:<div className='truncate px-2 text-primary font-semibold'>{workflow.datasetName}</div></div>

                    <div>Minimal FDs:<span className='px-2 text-primary font-semibold'>{111}</span></div>

                    <div>All FDs:<span className='px-2 text-primary font-semibold'>{222}</span></div>

                    <div />

                    <div>Total positive examples:<span className='px-2 text-primary font-semibold'>{111}</span></div>

                    <div>Total negative examples:<span className='px-2 text-primary font-semibold'>{222}</span></div>

                    <div>Total unanswered examples:<span className='px-2 text-primary font-semibold'>{333}</span></div>
                </CardBody>
            </Card>

            <Card>
                <CardHeader>
                    <h2 className='text-lg'>Genuine functional dependencies</h2>
                </CardHeader>

                <CardBody>
                    <p>Here you can see the list of all genuine functional dependencies you discovered.</p>

                    <div className='p-20 text-center text-lg text-danger'>TODO</div>
                </CardBody>
            </Card>

            <h2 className='mt-4'>Next steps</h2>

            <p>
                The functional dependency discovery process has finished. You can try again with a different dataset or approach!<br />
                We would also greatly appreciate your feedback on the results and the tool itself.
            </p>

            <div className='flex flex-col items-center gap-4'>
                <Button color='primary' className='min-w-80 md:w-1/2 lg:w-1/3' onPress={continueToWorkflow} isLoading={isFetching}>
                    Go again!
                </Button>

                <Button color='secondary' className='min-w-80 md:w-1/2 lg:w-1/3'>
                    Provide feedback
                </Button>
            </div>
        </div>
    );
}
