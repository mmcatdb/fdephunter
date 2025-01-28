import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { FDGraphDisplay, LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { WorkersDistribution } from '@/components/WorkersDistribution';
import { Class } from '@/types/workflow';
import { Card, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';
import { ArmstrongRelationDisplay, type WorkerOption } from '@/components/dataset/ArmstrongRelationDisplay';
import { Link, matchPath, Outlet, type Params, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from 'react-router';
import { type WorkflowLoaded } from './WorkflowPage';
import { routes } from '@/router';
import { useCallback, useMemo, useState } from 'react';
import { type ArmstrongRelation, MOCK_ARMSTRONG_RELATION } from '@/types/armstrongRelation';
import { Worker } from '@/types/worker';
import { API } from '@/utils/api';
import { JobResult } from '@/types/jobResult';
import { type DatasetData } from '@/types/dataset';
import { Approach } from '@/types/approach';
import { User } from '@/types/user';

export function WorkflowDashboardPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    return (<>
        <TopbarContent>
            <WorkflowDashboardTabs workflowId={workflow.id} />
        </TopbarContent>

        <Page>
            <Outlet />
        </Page>
    </>);
}

type WorkflowDashboardLoaded = {
    users: User[];
    workers: Worker[];
    approaches: Approach[];
    classes: Class[];
    jobResult: JobResult;
    data: DatasetData;
};

WorkflowDashboardPage.loader = async ({ params: { workflowId } }: { params: Params<'workflowId'> }): Promise<WorkflowDashboardLoaded> => {
    if (!workflowId)
        throw new Error('Missing workflow ID');

    const [ usersResponse, workersResponse, approachesResponse, classesResponse, jobResponse, dataResponse ] = await Promise.all([
        API.workers.getAllExpertUsers(undefined, {}),
        API.workflows.getAllWorkers(undefined, { workflowId }),
        API.approaches.getAll(undefined, {}),
        API.workflows.getClasses(undefined, { workflowId }),
        API.workflows.getLastJobResult(undefined, { workflowId }),
        API.datasets.getDataForWorkflow(undefined, { workflowId }),
    ]);
    if (!usersResponse.status)
        throw new Error('Failed to load users');
    if (!workersResponse.status)
        throw new Error('Failed to load workers');
    if (!approachesResponse.status)
        throw new Error('Failed to load approaches');
    if (!classesResponse.status)
        throw new Error('Failed to load classes');
    if (!jobResponse.status)
        throw new Error('Failed to load job result');
    if (!dataResponse.status)
        throw new Error('Failed to load dataset data');

    return {
        users: usersResponse.data.map(User.fromServer),
        workers: workersResponse.data.map(Worker.fromServer),
        approaches: approachesResponse.data.map(Approach.fromServer),
        classes: classesResponse.data.map(Class.fromServer),
        jobResult: JobResult.fromServer(jobResponse.data),
        data: dataResponse.data,
    };
};

function WorkflowDashboardTabs({ workflowId }: { workflowId: string }) {
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.workflow.dashboard.tabs.path, pathname)?.params.tab ?? 'index';

    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='index' title='Overview' {...{ as: Link, to: routes.workflow.dashboard.root.resolve({ workflowId }) }} />
            <Tab key='armstrong-relation' title='Armstrong relation' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'armstrong-relation' }) }} />
            <Tab key='dataset' title='Dataset' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'dataset' }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'list' }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.workflow.dashboard.tabs.resolve({ workflowId, tab: 'graph' }) }} />
        </Tabs>
    );
}

export function WorkersDistributionPage() {
    const revalidator = useRevalidator();
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const { users, workers, approaches, classes } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    const navigate = useNavigate();

    function handleNextStep() {
        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    const onWorkerCreated = useCallback(async (w: Worker) => {
        console.log('Worker created', w);
        await revalidator.revalidate();
    }, [ revalidator ]);

    return (
        <WorkersDistribution
            workflow={workflow}
            users={users}
            workers={workers}
            onWorkerCreated={onWorkerCreated}
            approaches={approaches}
            classes={classes}
            onNextStep={handleNextStep}
        />
    );
}

export function ArmstrongRelationPage() {
    const { workers } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    // FIXME Use real relation.
    const [ relation, setRelation ] = useState<ArmstrongRelation>(MOCK_ARMSTRONG_RELATION);

    // FIXME Synchronize with backend.
    const assignWorker = useCallback((rowIndex: number, workerId: string) => {
        setRelation(prev => ({
            ...prev,
            exampleRows: prev.exampleRows.map((row, i) => i === rowIndex ? { ...row, workerId } : row),
        }));
    }, []);

    const workerOptions = useMemo(() => workers.map(workerToOption), [ workers ]);

    return (
        <div className='flex flex-col items-start'>
            <Card className='p-4 max-w-full'>
                <ArmstrongRelationDisplay relation={relation} workerOptions={workerOptions} assignWorker={assignWorker} />
            </Card>
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
    const { data } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    return (
        <DatasetTable data={data} />
    );
}

export function WorkflowListPage() {
    const { jobResult } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    return (
        <FDListDisplay graph={jobResult.fdGraph} />
    );
}

export function WorkflowGraphPage() {
    const { jobResult } = useRouteLoaderData<WorkflowDashboardLoaded>(routes.workflow.dashboard.$id)!;

    return (
        // <FDGraphDisplay graph={jobResult.fdGraph} />
        <LatticeDisplay />
    );
}
