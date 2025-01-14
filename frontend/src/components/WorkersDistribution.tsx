import { useMemo, useState } from 'react';
import { routes, type NamedParams } from '@/router';
import { type User } from '@/types/user';
import { Worker } from '@/types/worker';
import { type WorkflowStats, type Workflow, type Class } from '@/types/workflow';
import { API } from '@/utils/api';
import { IoIosCheckmarkCircleOutline, IoIosCloseCircleOutline } from 'react-icons/io';
import { useUsers, useWorkers } from '@/hooks';
import { Link, useParams } from 'react-router-dom';
import { NegativeExampleState } from '@/types/negativeExample';
import { type IconType } from 'react-icons/lib';
import { IoReloadCircleOutline, IoStopCircleOutline } from 'react-icons/io5';
import { Button, Card, CardBody, CardFooter, CardHeader, Select, SelectItem, type SharedSelection } from '@nextui-org/react';

type WorkersDistributionProps = {
    workflow: Workflow;
    classes: Class[];
};

export function WorkersDistribution({ workflow, classes }: WorkersDistributionProps) {
    return (
        <div className='grid grid-cols-2 gap-4'>
            <div>
                <WorkflowStatsCard stats={MOCK_WORKFLOW_STATS} />
                <div className='mt-3'>
                    <WorkersOverviewCard />
                </div>
            </div>
            <div>
                <ClassStatsCard classes={classes} />
            </div>
        </div>
    );
}

const MOCK_WORKFLOW_STATS: WorkflowStats = {
    FDsInitial: 243957,
    FDsRemaining: 13401,
    examplesPositive: 33,
    examplesNegative: 256,
};

function WorkersOverviewCard() {
    const { workflowId } = useParams() as NamedParams<typeof routes.workflow.detail>;

    const { workers, setWorkers } = useWorkers(workflowId);

    function workerCreated(worker: Worker) {
        setWorkers([ ...workers, worker ]);
    }

    return (
        <Card>
            <CardHeader>
                Workers
            </CardHeader>
            <CardBody>
                <WorkersTable workers={workers} />
            </CardBody>
            <CardFooter>
                <AddWorker onCreated={workerCreated} />
            </CardFooter>
        </Card>
    );
}

type WorkersTableProps = {
    workers: Worker[];
};

function WorkersTable({ workers }: WorkersTableProps) {
    return (<>
        <div className='grid grid-cols-2 gap-4 font-bold'>
            <div>User</div>
            <div className='text-center'>Link</div>
        </div>

        {workers.map(worker => (
            <div key={worker.id} className='grid grid-cols-2 gap-4'>
                <div>{worker.user.name}</div>
                <div className='text-center'>
                    <Link to={routes.worker.detail.resolve({ workerId: worker.id })} target='_blank' rel='noreferrer'>
                        {routes.worker.detail.resolve({ workerId: worker.id })}
                    </Link>
                </div>
            </div>
        ))}
    </>);
}

type AddWorkerProps = {
    onCreated: (worker: Worker) => void;
};

function AddWorker({ onCreated }: AddWorkerProps) {
    const users = useUsers();
    const [ selectedUser, setSelectedUser ] = useState(new Set<string>());
    const userOptions = useMemo(() => users?.map(userToOption), [ users ]);
    const [ fetching, setFetching ] = useState(false);

    const { workflowId } = useParams() as NamedParams<typeof routes.workflow.detail>;

    async function submit() {
        const selectedUserId = selectedUser.values().next().value;
        const user = users?.find(u => u.id === selectedUserId);
        if (!user)
            return;

        setFetching(true);
        const response = await API.workflows.addWorker({ workflowId }, {
            userId: user.id,
        });
        setFetching(false);
        if (!response.status)
            return;

        onCreated(Worker.fromServer(response.data));
        setSelectedUser(new Set());
    }

    if (!userOptions)
        return null;

    return (
        <div className='pb-1 grid grid-cols-3 gap-4'>
            <div className='col-span-2'>
                <Select
                    label='Add another user'
                    selectionMode='single'
                    items={userOptions}
                    selectedKeys={selectedUser}
                    onSelectionChange={setSelectedUser as (keys: SharedSelection) => void}
                >
                    {option => (
                        <SelectItem key={option.key}>{option.label}</SelectItem>
                    )}
                </Select>
            </div>

            <div className='flex items-end'>
                <Button
                    className='w-full'
                    onPress={submit}
                    isLoading={fetching}
                >
                    Add
                </Button>
            </div>
        </div>
    );
}

type Option = {
    key: string;
    label: string;
};

function userToOption(user: User): Option {
    return {
        key: user.id,
        label: user.name,
    };
}

type WorkflowStatsCardProps = {
    stats: WorkflowStats;
};

function WorkflowStatsCard({ stats }: WorkflowStatsCardProps) {
    return (
        <Card>
            <CardHeader>
                Workflow statistics
            </CardHeader>
            <CardBody>
                <div className='grid grid-cols-2 gap-4 font-bold'>
                    <div className='w-1/2'>Functional dependencies</div>
                    <div className='w-1/2'>Evaluated examples</div>
                </div>
                <div className='grid grid-cols-2 gap-4'>
                    <div className='w-1/2'>Initial: {stats.FDsInitial}</div>
                    <div className='w-1/2'>Positive: {stats.examplesPositive}</div>
                </div>
                <div className='grid grid-cols-2 gap-4'>
                    <div className='w-1/2'>Remaining: {stats.FDsRemaining}</div>
                    <div className='w-1/2'>Negative: {stats.examplesNegative}</div>
                </div>
            </CardBody>
        </Card>
    );
}

type ClassStatsCardProps = {
    classes: Class[];
};

function ClassStatsCard({ classes }: ClassStatsCardProps) {
    console.log(classes);

    return (
        <Card>
            <CardHeader>
                Class statistics
            </CardHeader>
            <CardBody>
                <div className='font-bold grid grid-cols-6 gap-4'>
                    <div className='col-span-3'>Name</div>
                    <div className='text-center'>Weight</div>
                    <div className='text-center'>Iteration</div>
                    <div className='text-center'>State</div>
                </div>

                {classes.map(c => (
                    <div key={c.id} className='grid grid-cols-6 gap-4'>
                        <div className='col-span-3'>{c.label}</div>
                        <div className='text-center'>{c.weight}</div>
                        <div className='text-center'>{c.iteration}</div>
                        <div className='text-center'>{c.example && <ExampleStateIcon state={c.example.state} />}</div>
                    </div>
                ))}
            </CardBody>
        </Card>
    );
}

type ExampleStateIconProps = {
    state: NegativeExampleState;
};

export function ExampleStateIcon({ state }: ExampleStateIconProps) {
    const data = exampleStateData[state];

    return (
        <span className={`text-${data.color}`}>
            {data.icon({ size: 24 })}
        </span>
    );
}

const exampleStateData: {
    [key in NegativeExampleState]: {
        color: string;
        icon: IconType;
    }
} = {
    [NegativeExampleState.New]: { icon: IoReloadCircleOutline, color: 'text-info' },
    [NegativeExampleState.Rejected]: { icon: IoIosCloseCircleOutline, color: 'text-danger' },
    [NegativeExampleState.Accepted]: { icon: IoIosCheckmarkCircleOutline, color: 'text-success' },
    [NegativeExampleState.Answered]: { icon: IoReloadCircleOutline, color: 'text-info' },
    [NegativeExampleState.Conflict]: { icon: IoStopCircleOutline, color: 'text-warning' },
};
