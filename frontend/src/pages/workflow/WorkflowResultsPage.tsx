import { Button, Card, CardBody, CardHeader, Tab, Tabs } from '@heroui/react';
import { type WorkflowLoaded } from './WorkflowPage';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRouteLoaderData } from 'react-router';
import { routes } from '@/router';
import { Page, TopbarContent } from '@/components/layout';
import { useMemo, useState } from 'react';
import { Workflow } from '@/types/workflow';
import { FdSet, type FdEdge } from '@/types/functionalDependency';
import { FdListDisplay } from '@/components/dataset/FdListDisplay';
import { type Id } from '@/types/id';
import { API } from '@/utils/api/api';
import { type DatasetResponse } from '@/types/dataset';

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

function WorkflowResultsTabs({ workflowId }: { workflowId: Id }) {
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
    const { fdSet, dataset } = useLoaderData<WorkflowFinalLoaded>();

    const [ isFetching, setIsFetching ] = useState(false);

    async function continueToWorkflow() {
        setIsFetching(true);
        const response = await API.workflow.createWorkflow({});
        setIsFetching(false);
        if (!response.status)
            return;

        window.open(routes.workflow.settings.resolve({ workflowId: Workflow.fromResponse(response.data).id }), '_blank');
    }

    const fds = useMemo(() => createFdEdges(fdSet), [ fdSet ]);

    return (
        <div className='mx-auto w-fit flex flex-col gap-8'>
            <Card className='w-full'>
                <CardHeader>
                    <h1 className='text-lg'>Final results</h1>
                </CardHeader>

                <CardBody className='grid grid-cols-3 gap-x-8 gap-y-2'>
                    <div>Total iterations:<span className='px-2 text-primary font-semibold'>{workflow.totalIterations}</span></div>

                    <div className='col-span-2 flex items-center'>Dataset:<div className='truncate px-2 text-primary font-semibold'>{dataset.name}</div></div>

                    <div>Minimal FDs:<span className='px-2 text-primary font-semibold'>{4}</span></div>

                    <div>All FDs:<span className='px-2 text-primary font-semibold'>{28}</span></div>

                    <div />

                    <div>Total negative examples:<span className='px-2 text-primary font-semibold'>{11}</span></div>

                    <div>Total positive examples:<span className='px-2 text-primary font-semibold'>{2}</span></div>

                    <div>Total unanswered examples:<span className='px-2 text-primary font-semibold'>{0}</span></div>
                </CardBody>
            </Card>

            <Card>
                <CardHeader>
                    <h2 className='text-lg'>Genuine functional dependencies</h2>
                </CardHeader>

                <CardBody className='space-y-4'>
                    <p>Here you can see the list of all genuine functional dependencies you discovered:</p>

                    <FdListDisplay edges={fds} />
                </CardBody>
            </Card>

            <h2 className='mt-4'>Next steps</h2>

            <p>
                The functional dependency discovery process has finished. You can try again with a different dataset!<br />
                We would also greatly appreciate your feedback on the results and the tool itself.
            </p>

            <div className='flex flex-col items-center gap-4'>
                <Button color='primary' className='min-w-80 md:w-1/2 lg:w-1/3' onPress={continueToWorkflow} isLoading={isFetching}>
                    Go again!
                </Button>

                {/* TODO */}
                {/* <Button color='secondary' className='min-w-80 md:w-1/2 lg:w-1/3'>
                    Provide feedback
                </Button> */}
            </div>
        </div>
    );
}

type WorkflowFinalLoaded = {
    fdSet: FdSet;
    dataset: DatasetResponse;
};

WorkflowFinalPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowFinalLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const [ fdSetResponse, datasetResponse ] = await Promise.all([
        API.view.getFds(undefined, { workflowId }),
        API.dataset.getDataset(undefined, { workflowId }),
    ]);
    if (!fdSetResponse.status)
        throw new Error('Failed to load functional dependencies');
    if (!datasetResponse.status)
        throw new Error('Failed to load dataset');

    return {
        fdSet: FdSet.fromResponse(fdSetResponse.data),
        dataset: datasetResponse.data,
    };
};

export function createFdEdges(fdSet: FdSet): FdEdge[] {
    console.log(fdSet);
    return fdSet.fds.flatMap(fd => fd.rhs.columns.map(colIndex => ({
        id: fd.lhs.id + '->' + colIndex,
        source: {
            columns: fd.lhs.map(fdSet.columns),
            label: fd.lhs.toString(fdSet.columns),
            id: fd.lhs.id,
        },
        target: {
            columns: [ fdSet.columns[colIndex] ],
            label: fdSet.columns[colIndex],
            id: colIndex.toString(),
        },
    })));
}
