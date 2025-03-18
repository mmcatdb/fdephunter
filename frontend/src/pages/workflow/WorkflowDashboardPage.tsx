import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { WorkersDistribution } from '@/components/WorkersDistribution';
import { Class } from '@/types/workflow';
import { Button, Card, CardBody, CardFooter, CardHeader, Divider, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';
import { ArmstrongRelationDisplay, type WorkerOption } from '@/components/dataset/ArmstrongRelationDisplay';
import { Link, matchPath, Outlet, type Params, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from 'react-router';
import { type WorkflowLoaded } from './WorkflowPage';
import { routes } from '@/router';
import { useCallback, useMemo, useState } from 'react';
import { ExampleState, MOCK_LATTICES, type ArmstrongRelation } from '@/types/armstrongRelation';
import { type Worker } from '@/types/worker';
import { API } from '@/utils/api';
import { JobResult } from '@/types/jobResult';
import { type DatasetData } from '@/types/dataset';
import { Approach } from '@/types/approach';
import { User } from '@/types/user';
import clsx from 'clsx';
import { mockAPI } from '@/utils/api/mockAPI';
import { type AssignmentInfo } from '@/types/assignment';

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
    // users: User[];
    // workers: Worker[];
    // approaches: Approach[];
    // classes: Class[];
    jobResult: JobResult;
    assignments: AssignmentInfo[];
    // data: DatasetData;
};

WorkflowDashboardPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowDashboardLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    // const [ usersResponse, workersResponse, approachesResponse, classesResponse, jobResponse, dataResponse ] = await Promise.all([
    //     API.workers.getAllExpertUsers(undefined, {}),
    //     API.workflows.getAllWorkers(undefined, { workflowId }),
    //     API.approaches.getAll(undefined, {}),
    //     API.workflows.getClasses(undefined, { workflowId }),
    //     API.workflows.getLastJobResult(undefined, { workflowId }),
    //     API.datasets.getDataForWorkflow(undefined, { workflowId }),
    // ]);
    // if (!usersResponse.status)
    //     throw new Error('Failed to load users');
    // if (!workersResponse.status)
    //     throw new Error('Failed to load workers');
    // if (!approachesResponse.status)
    //     throw new Error('Failed to load approaches');
    // if (!classesResponse.status)
    //     throw new Error('Failed to load classes');
    // if (!jobResponse.status)
    //     throw new Error('Failed to load job result');
    // if (!dataResponse.status)
    //     throw new Error('Failed to load dataset data');

    // return {
    //     users: usersResponse.data.map(User.fromServer),
    //     workers: workersResponse.data.map(Worker.fromServer),
    //     approaches: approachesResponse.data.map(Approach.fromServer),
    //     classes: classesResponse.data.map(Class.fromServer),
    //     jobResult: JobResult.fromServer(jobResponse.data),
    //     data: dataResponse.data,
    // };

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

// export function WorkersDistributionPage() {
//     const revalidator = useRevalidator();
//     const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
//     const { users, workers, approaches, classes } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

//     const navigate = useNavigate();

//     function handleNextStep() {
//         void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
//     }

//     const onWorkerCreated = useCallback(async (w: Worker) => {
//         console.log('Worker created', w);
//         await revalidator.revalidate();
//     }, [ revalidator ]);

//     return (
//         <WorkersDistribution
//             workflow={workflow}
//             users={users}
//             workers={workers}
//             onWorkerCreated={onWorkerCreated}
//             approaches={approaches}
//             classes={classes}
//             onNextStep={handleNextStep}
//         />
//     );
// }

export function WorkflowOverviewPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    // const { workers } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;
    const { jobResult, assignments } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // FIXME Use real relation.
    const relation = jobResult.relation;

    const navigate = useNavigate();
    const [ fetching, setFetching ] = useState<string>();

    // FIXME Synchronize with backend.
    const assignWorker = useCallback(async (rowIndex: number, workerId?: string) => {
        if (workerId)
            return;

        const assignment = await mockAPI.assignments.create({
            workerId: undefined,
            workflowId: workflow.id,
            rowIndex,
        });
        if (!assignment.status)
            return;

        void navigate(routes.assignment.root.resolve({ assignmentId: assignment.data.id }));

        // setRelation(prev => ({
        //     ...prev,
        //     exampleRows: prev.exampleRows.map((row, i) => i === rowIndex ? { ...row, workerId } : row),
        // }));
    }, []);

    // const workerOptions = useMemo(() => workers.map(workerToOption), [ workers ]);
    const workerOptions = useMemo(() => [], []);

    async function runRediscovery() {
        setFetching(FID_CONTINUE);
        // const response = await API.workflows.executeRediscovery({ workflowId: workflow.id }, {
        const response = await mockAPI.workflows.executeRediscovery(workflow.id, {
            // approach: approach.name,
            approach: 'HyFD',
        });
        if (!response.status) {
            setFetching(undefined);
            return;
        }

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
        const evaluated = relation.exampleRows.filter(row => row.state !== ExampleState.New).length;

        return {
            positive,
            negative,
            evaluated,
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

                    <div>Unanswered examples:<span className='px-2 text-primary font-semibold'>{stats.evaluated}</span></div>
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
                <ArmstrongRelationDisplay relation={relation} workerOptions={workerOptions} assignWorker={assignWorker} assignments={assignments} />
            </Card>
        </div>
    );
}

const FID_CONTINUE = 'continue';
const FID_ACCEPT_ALL = 'accept-all';

function workerToOption(worker: Worker): WorkerOption {
    return {
        key: worker.id,
        label: worker.user.name,
    };
}

export function WorkflowDatasetPage() {
    // const { data } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // return (
    //     <DatasetTable data={data} />
    // );

    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const navigate = useNavigate();

    const revalidate = useRevalidator();

    async function forceContinue() {
        const response = await mockAPI.workflows.executeRediscovery(workflow.id, { approach: 'HyFD' }, true);
        if (!response.status)
            return;

        await revalidate.revalidate();
        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    return (
        <div>
            <Button color='primary' onPress={forceContinue}>
                Fast continue
            </Button>
        </div>
    );
}

export function WorkflowListPage() {
    // const { jobResult } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // return (
    //     <FDListDisplay graph={jobResult.fdGraph.edges} />
    // );

    return (
        <div />
    );
}

export function WorkflowGraphPage() {
    // const { jobResult } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const index = workflow.iteration === 0 ? 0 : 1;

    return (
        <LatticeDisplay lattices={MOCK_LATTICES[index]} />
    );
}
