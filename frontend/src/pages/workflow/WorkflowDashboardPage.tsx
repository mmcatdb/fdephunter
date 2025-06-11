import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { FDListDisplayBalanced } from '@/components/dataset/FDListDisplay';
import { LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { Button, Card, CardBody, CardFooter, CardHeader, Tab, Tabs } from '@heroui/react';
import { Page, TopbarContent } from '@/components/layout';
import { ArmstrongRelationDisplay } from '@/components/dataset/ArmstrongRelationDisplay';
import { Link, matchPath, Outlet, type Params, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from 'react-router';
import { type WorkflowLoaded } from './WorkflowPage';
import { routes } from '@/router';
import { useCallback, useMemo, useState } from 'react';
import { ExampleState } from '@/types/armstrongRelation';
import clsx from 'clsx';
import { mockAPI } from '@/utils/api/mockAPI';
import { type AssignmentInfo } from '@/types/assignment';
import { createFdEdges } from './WorkflowResultsPage';
import { JobResult } from '@/types/job';

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
    jobResult: JobResult;
    assignments: AssignmentInfo[];
};

WorkflowDashboardPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowDashboardLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const [ jobResponse, assignmentsResponse ] = await Promise.all([
        mockAPI.workflows.getLastJobResult(workflowId),
        mockAPI.assignments.getAllAssignments(workflowId),
    ]);

    if (!jobResponse.status)
        throw new Error('Failed to load job result');
    if (!assignmentsResponse.status)
        throw new Error('Failed to load assignments');

    return {
        jobResult: JobResult.fromServer(jobResponse.data),
        assignments: assignmentsResponse.data,
    };
};

function WorkflowDashboardTabs({ workflowId, selectedKey }: { workflowId: string, selectedKey: string }) {
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
    const { jobResult, assignments } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // FIXME Use real relation.
    const relation = jobResult.relation;

    const navigate = useNavigate();
    const [ fetching, setFetching ] = useState<string>();

    // FIXME Synchronize with backend.
    const assignRow = useCallback(async (rowIndex: number) => {
        const assignment = await mockAPI.assignments.create({
            workflowId: workflow.id,
            rowIndex,
        });
        if (!assignment.status)
            return;

        void navigate(routes.assignment.root.resolve({ assignmentId: assignment.data.id }));

        // TODO Is this needed?
        // setRelation(prev => ({
        //     ...prev,
        //     exampleRows: prev.exampleRows.map((row, i) => i === rowIndex ? { ...row, workerId } : row),
        // }));
    }, []);

    async function runRediscovery() {
        setFetching(FID_CONTINUE);
        // const response = await API.workflows.executeRediscovery({ workflowId: workflow.id }, {
        const response = await mockAPI.workflows.executeRediscovery(workflow.id, {
            // approach,
            approach: 'HyFD',
        });
        if (!response.status) {
            setFetching(undefined);
            return;
        }

        // TODO Is this needed?
        // onNextStep(
        //     Workflow.fromServer(response.data.workflow),
        //     Job.fromServer(response.data.job),
        // );

        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    const revalidator = useRevalidator();

    async function acceptAll() {
        setFetching(FID_ACCEPT_ALL);
        const response = await mockAPI.workflows.acceptAllExamples(workflow.id);
        if (!response.status) {
            setFetching(undefined);
            return;
        }

        await revalidator.revalidate();
        setFetching(undefined);
    }

    const isContinueEnabled = useMemo(() => relation.exampleRows.filter(row => row.isPositive === relation.isEvaluatingPositives).every(row => row.state !== ExampleState.New), [ relation ]);
    const isAcceptAllEnabled = !isContinueEnabled;

    const stats = useMemo(() => {
        const positive = relation.exampleRows.filter(row => row.isPositive).length;
        const negative = relation.exampleRows.filter(row => !row.isPositive).length;
        const unanswered = relation.exampleRows.filter(row => row.isPositive === relation.isEvaluatingPositives && [ ExampleState.New, ExampleState.Undecided ].includes(row.state)).length;

        return {
            positive,
            negative,
            unanswered,
        };
    }, [ relation ]);

    return (
        <div className='mx-auto w-fit flex flex-col gap-8'>
            <Card className='w-full'>
                <CardHeader>
                    <h1 className='text-lg'>Workflow overview</h1>
                </CardHeader>

                <CardBody className='grid grid-cols-3 gap-x-8 gap-y-2'>
                    <div>Iteration:<span className='px-2 text-primary font-semibold'>{workflow.iteration}</span></div>

                    <div className='col-span-2 flex items-center'>Dataset:<div className='truncate px-2 text-primary font-semibold'>{workflow.datasetName}</div></div>

                    <div>Minimal FDs:<span className='px-2 text-primary font-semibold'>{relation.minimalFDs}</span></div>

                    <div>All FDs:<span className='px-2 text-primary font-semibold'>{relation.minimalFDs + relation.otherFDs}</span></div>

                    <div>LHS size:<span className='px-2 text-primary font-semibold'>{relation.lhsSize}</span></div>

                    <div>Negative examples:<span className='px-2 text-primary font-semibold'>{stats.negative}</span></div>

                    <div>Positive examples:<span className='px-2 text-primary font-semibold'>{stats.positive}</span></div>

                    <div>Unanswered examples:<span className='px-2 text-primary font-semibold'>{stats.unanswered}</span></div>
                </CardBody>

                <CardFooter className='flex justify-end gap-4'>
                    <Button color='primary' onPress={runRediscovery} isDisabled={!isContinueEnabled || !!fetching} isLoading={fetching === FID_CONTINUE}>
                        Continue
                    </Button>
                    <Button color='secondary' onPress={acceptAll} isDisabled={!isAcceptAllEnabled || !!fetching} isLoading={fetching === FID_ACCEPT_ALL}>
                        Accept all
                    </Button>
                </CardFooter>
            </Card>

            <Card className='max-w-full p-4 items-center'>
                <ArmstrongRelationDisplay relation={relation} assignRow={assignRow} assignments={assignments} />
            </Card>
        </div>
    );
}

const FID_CONTINUE = 'continue';
const FID_ACCEPT_ALL = 'accept-all';

export function WorkflowDatasetPage() {
    const { dataset } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    return (
        <DatasetTable data={dataset} />
    );
}

export function WorkflowListPage() {
    const { workflow, fdClasses, jobResult } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    const index = workflow.iteration === 0 ? 0 : 1;
    const fds = useMemo(() => createFdEdges(fdClasses, jobResult!.relation.columns), [ index ]);

    return (
        <Card className='mx-auto w-fit'>
            <CardBody>
                <FDListDisplayBalanced edges={fds} />
            </CardBody>
        </Card>
    );
}

export function WorkflowGraphPage() {
    const { lattices } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    return (
        <LatticeDisplay lattices={lattices} />
    );
}
