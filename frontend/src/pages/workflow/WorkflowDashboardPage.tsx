import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { WorkersDistribution } from '@/components/WorkersDistribution';
import { Class } from '@/types/workflow';
import { Button, Card, CardBody, Tab, Tabs } from '@nextui-org/react';
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

        <Page className={clsx(selectedKey === 'graph' && 'max-w-[unset] min-w-[800px] min-h-[600px] h-full pr-0 pb-0')}>
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
            {/* <Tab key='armstrong-relation' title='Armstrong relation' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'armstrong-relation' }) }} /> */}
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

export function ArmstrongRelationPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    // const { workers } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;
    const { jobResult, assignments } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // FIXME Use real relation.
    const relation = jobResult.relation;

    const navigate = useNavigate();
    const [ isFetching, setIsFetching ] = useState(false);

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
        setIsFetching(true);
        // const response = await API.workflows.executeRediscovery({ workflowId: workflow.id }, {
        const response = await mockAPI.workflows.executeRediscovery(workflow.id, {
            // approach: approach.name,
            approach: 'HyFD',
        });
        if (!response.status) {
            setIsFetching(false);
            return;
        }

        // onNextStep(
        //     Workflow.fromServer(response.data.workflow),
        //     Job.fromServer(response.data.job),
        // );

        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    const isContinueEnabled = useMemo(() => relation.exampleRows.filter(row => row.isNegative || relation.isPositivesAllowed).every(row => row.state !== ExampleState.New), [ relation ]);

    const stats = useMemo(() => {
        const positive = relation.exampleRows.filter(row => !row.isNegative).length;
        const negative = relation.exampleRows.filter(row => row.isNegative).length;
        const evaluated = relation.exampleRows.filter(row => row.state !== ExampleState.New).length;

        return {
            positive,
            negative,
            evaluated,
        };
    }, [ relation ]);

    return (
        <div className='mx-auto w-fit flex flex-col items-end gap-8'>
            <Card className='w-full'>
                <CardBody className='grid grid-flow-col gap-8'>
                    <div>
                        Positive FDs: <span className='px-1 text-primary font-semibold'>{stats.positive}</span>
                    </div>

                    <div>
                        Negative FDs: <span className='px-1 text-primary font-semibold'>{stats.negative}</span>
                    </div>

                    <div>
                        Evaluated FDs: <span className='px-1 text-primary font-semibold'>{stats.evaluated}</span>
                    </div>
                </CardBody>
            </Card>

            <Card className='max-w-full p-4'>
                <ArmstrongRelationDisplay relation={relation} workerOptions={workerOptions} assignWorker={assignWorker} assignments={assignments} />
            </Card>

            <Button color='primary' onPress={runRediscovery} isDisabled={!isContinueEnabled} isLoading={isFetching}>
                Continue
            </Button>
        </div>
    );
}

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

    async function forceContinue() {
        const response = await mockAPI.workflows.executeRediscovery(workflow.id, { approach: 'HyFD' }, true);
        if (!response.status)
            return;

        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    return (
        <div>
            <Button color='primary' onPress={forceContinue}>
                Fast continue
            </Button>

            <div className='mt-8'>
                The left panel might display incorrect data after this operation. You can fix it by refreshing the page.
            </div>
        </div>
    );
}

export function WorkflowListPage() {
    // const { jobResult } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // return (
    //     <FDListDisplay graph={jobResult.fdGraph} />
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
        <LatticeDisplay lattice={MOCK_LATTICES[index]} />
    );
}
