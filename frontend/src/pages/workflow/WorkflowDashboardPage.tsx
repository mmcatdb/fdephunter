import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { FdListDisplay } from '@/components/dataset/FdListDisplay';
import { LatticeDisplay } from '@/components/dataset/FdGraphDisplay';
import { Button, Card, CardBody, CardFooter, CardHeader, Tab, Tabs } from '@heroui/react';
import { Page, TopbarContent } from '@/components/layout';
import { AssignmentsDisplay } from '@/components/dataset/AssignmentsDisplay';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from 'react-router';
import { type WorkflowLoaded } from './WorkflowPage';
import { routes } from '@/router';
import { useMemo, useState } from 'react';
import { DecisionStatus, type Lattice } from '@/types/examples';
import clsx from 'clsx';
import { Assignment } from '@/types/assignment';
import { createFdEdges } from './WorkflowResultsPage';
import { type Id } from '@/types/id';
import { type DatasetResponse, type DatasetData } from '@/types/dataset';
import { FdSet } from '@/types/functionalDependency';
import { API } from '@/utils/api/api';

export function WorkflowDashboardPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.workflow.dashboard.tabs.path, pathname)?.params.tab ?? 'index';

    return (<>
        <TopbarContent>
            <WorkflowDashboardTabs workflowId={workflow.id} selectedKey={selectedKey} />
        </TopbarContent>

        <Page className={clsx(selectedKey === 'graph' && 'max-w-[unset] min-w-[800px] min-h-[600px] h-full pb-0')}>
            <Outlet />
        </Page>
    </>);
}

type WorkflowDashboardLoaded = {
    assignments: Assignment[];
    dataset: DatasetResponse;
};

WorkflowDashboardPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowDashboardLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const [ assignmentsResponse, datasetResponse ] = await Promise.all([
        API.assignment.getAssignments(undefined, { workflowId }),
        API.dataset.getDataset(undefined, { workflowId }),
    ]);

    if (!assignmentsResponse.status)
        throw new Error('Failed to load assignments');
    if (!datasetResponse.status)
        throw new Error('Failed to load dataset');

    return {
        assignments: assignmentsResponse.data.map(Assignment.fromResponse),
        dataset: datasetResponse.data,
    };
};

function WorkflowDashboardTabs({ workflowId, selectedKey }: { workflowId: Id, selectedKey: string }) {
    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='index' title='Overview' {...{ as: Link, to: routes.workflow.dashboard.root.resolve({ workflowId }) }} />
            <Tab key='dataset' title='Dataset' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'dataset' }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'list' }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'graph' }) }} />
        </Tabs>
    );
}

export function WorkflowOverviewPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const { assignments, dataset } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    const navigate = useNavigate();
    const [ fetching, setFetching ] = useState<string>();

    async function runRediscovery() {
        setFetching(FID_CONTINUE);
        const response = await API.workflow.continueWorkflow({ workflowId: workflow.id });
        if (!response.status) {
            setFetching(undefined);
            return;
        }

        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    const revalidator = useRevalidator();

    async function acceptAll() {
        setFetching(FID_ACCEPT_ALL);
        const response = await API.workflow.acceptAllAssignments({ workflowId: workflow.id });
        if (!response.status) {
            setFetching(undefined);
            return;
        }

        await revalidator.revalidate();
        setFetching(undefined);
    }

    const isContinueEnabled = useMemo(() => assignments
        .filter(a => a.exampleRow.isEvaluationAllowed(workflow))
        .every(a => a.exampleRow.decision && a.exampleRow.decision.status !== DecisionStatus.Unanswered),
    [ assignments, workflow ]);
    const isAcceptAllEnabled = !isContinueEnabled;

    const stats = useMemo(() => {
        const positive = assignments.filter(a => a.exampleRow.isPositive).length;
        const negative = assignments.filter(a => !a.exampleRow.isPositive).length;
        const unanswered = assignments
            .filter(a => a.exampleRow.isEvaluationAllowed(workflow))
            .filter(a => !a.exampleRow.decision || a.exampleRow.decision.status === DecisionStatus.Unanswered)
            .length;

        return {
            positive,
            negative,
            unanswered,
            lhsSize: workflow.lhsSize,
        };
    }, [ workflow, assignments ]);

    return (
        <div className='mx-auto max-w-full w-fit flex flex-col gap-8'>
            <Card className='w-full'>
                <CardHeader>
                    <h1 className='text-lg'>Workflow overview</h1>
                </CardHeader>

                <CardBody className='grid grid-cols-3 gap-x-8 gap-y-2'>
                    <div>LHS size:<span className='px-2 text-primary font-semibold'>{workflow.lhsSize}</span></div>

                    <div className='col-span-2 flex items-center'>Dataset:<div className='truncate px-2 text-primary font-semibold'>{dataset.name}</div></div>

                    <div>Minimal FDs:<span className='px-2 text-primary font-semibold'>{workflow.minimalFds}</span></div>

                    <div>All FDs:<span className='px-2 text-primary font-semibold'>{workflow.totalFds}</span></div>

                    <div>LHS size:<span className='px-2 text-primary font-semibold'>{stats.lhsSize}</span></div>

                    <div>Negative examples:<span className='px-2 text-primary font-semibold'>{stats.negative}</span></div>

                    <div>Positive examples:<span className='px-2 text-primary font-semibold'>{stats.positive}</span></div>

                    <div>Unanswered examples:<span className='px-2 text-primary font-semibold'>{stats.unanswered}</span></div>
                </CardBody>

                <CardFooter className='flex justify-end gap-4'>
                    <Button color='primary' onPress={runRediscovery} isDisabled={!isContinueEnabled || !!fetching} isLoading={fetching === FID_CONTINUE}>
                        Continue
                    </Button>
                    <Button color='secondary' onPress={acceptAll} isDisabled={!isAcceptAllEnabled || !!fetching} isLoading={fetching === FID_ACCEPT_ALL}>
                        Accept all remaining
                    </Button>
                </CardFooter>
            </Card>

            <Card className='max-w-full p-4 items-center'>
                <AssignmentsDisplay workflow={workflow} assignments={assignments} />
            </Card>
        </div>
    );
}

const FID_CONTINUE = 'continue';
const FID_ACCEPT_ALL = 'accept-all';

export function WorkflowDatasetPage() {
    const { dataset } = useLoaderData<WorkflowDatasetLoaded>();

    return (
        <DatasetTable data={dataset} />
    );
}

type WorkflowDatasetLoaded = {
    dataset: DatasetData;
};

WorkflowDatasetPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowDatasetLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const response = await API.dataset.getDatasetData(undefined, { workflowId });
    if (!response.status)
        throw new Error('Failed to load dataset data');

    return { dataset: response.data };
};

export function WorkflowListPage() {
    const { fdSet } = useLoaderData<WorkflowListLoaded>();

    const fds = useMemo(() => createFdEdges(fdSet), [ fdSet ]);

    return (
        <Card className='mx-auto w-fit'>
            <CardBody>
                <FdListDisplay edges={fds} />
            </CardBody>
        </Card>
    );
}

type WorkflowListLoaded = {
    fdSet: FdSet;
};

WorkflowListPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowListLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const response = await API.view.getFds(undefined, { workflowId });
    if (!response.status)
        throw new Error('Failed to load functional dependencies');

    return { fdSet: FdSet.fromResponse(response.data) };
};

export function WorkflowGraphPage() {
    const { lattices } = useLoaderData<WorkflowGraphLoaded>();

    return (
        <LatticeDisplay lattices={lattices} />
    );
}

type WorkflowGraphLoaded = {
    lattices: Lattice[];
};

WorkflowGraphPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowGraphLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const response = await API.view.getLattices(undefined, { workflowId });
    if (!response.status)
        throw new Error('Failed to load lattices');

    return { lattices: response.data.lattices };
};
