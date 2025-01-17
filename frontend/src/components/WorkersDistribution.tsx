import { useMemo, useState } from 'react';
import { routes, type NamedParams } from '@/router';
import { type User } from '@/types/user';
import { Worker } from '@/types/worker';
import { Workflow, type WorkflowStats, type Class } from '@/types/workflow';
import { API } from '@/utils/api';
import { IoIosCheckmarkCircleOutline, IoIosCloseCircleOutline } from 'react-icons/io';
import { useApproaches, useUsers, useWorkers } from '@/hooks';
import { Link, useParams } from 'react-router-dom';
import { ExampleState } from '@/types/negativeExample';
import { type IconType } from 'react-icons/lib';
import { IoReloadCircleOutline, IoStopCircleOutline } from 'react-icons/io5';
import { Button, Card, CardBody, CardFooter, CardHeader, Divider, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Select, SelectItem, type SharedSelection } from '@nextui-org/react';
import { nameToOption } from '@/pages/InitialSettings';
import { type Approach } from '@/types/approach';
import { Job } from '@/types/job';

type WorkersDistributionProps = {
    workflow: Workflow;
    classes: Class[];
    onNextStep: (workflow: Workflow, job: Job) => void;
};

export function WorkersDistribution({ workflow, classes, onNextStep }: WorkersDistributionProps) {
    const canGoNext = useMemo(() => {
        const acceptedClasses = classes.filter(c => c.example && c.example.state === ExampleState.Accepted);
        return classes.length === acceptedClasses.length;
    }, [ classes ]);

    const [ showModal, setShowModal ] = useState(false);
    const [ fetching, setFetching ] = useState(false);

    async function runRediscovery(approach: Approach) {
        setFetching(true);
        const response = await API.workflows.executeRediscovery({ workflowId: workflow.id }, {
            approach: approach.name,
        });
        setFetching(false);
        if (!response.status)
            return;

        onNextStep(
            Workflow.fromServer(response.data.workflow),
            Job.fromServer(response.data.job),
        );
    }

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

            <div className='mt-10 col-span-2 flex justify-end'>
                <Button color='primary' onPress={() => setShowModal(true)} isDisabled={!canGoNext}>
                    Go next
                </Button>
            </div>

            <Modal isOpen={showModal} onClose={() => setShowModal(false)}>
                <RediscoveryFormModal onSubmit={runRediscovery} fetching={fetching} />
            </Modal>
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
                <h3 className='font-semibold'>Workers</h3>
            </CardHeader>

            <Divider />

            <CardBody>
                <WorkersTable workers={workers} />
            </CardBody>

            <Divider />

            <CardFooter className='gap-6'>
                <AddWorker workers={workers} onCreated={workerCreated} />
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
                    <Link to={routes.worker.detail.resolve({ workerId: worker.id })} target='_blank' rel='noreferrer' className='text-primary hover:underline'>
                        {routes.worker.detail.resolve({ workerId: worker.id })}
                    </Link>
                </div>
            </div>
        ))}
    </>);
}

type AddWorkerProps = {
    workers: Worker[];
    onCreated: (worker: Worker) => void;
};

function AddWorker({ workers, onCreated }: AddWorkerProps) {
    const users = useUsers();
    const [ selectedUser, setSelectedUser ] = useState(new Set<string>());
    const userOptions = useMemo(() => users?.filter(user => !workers.some(worker => worker.user.id === user.id)).map(userToOption), [ users, workers ]);
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

    return (<>
        <Select
            size='sm'
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

        <Button
            className='shrink-0 w-1/3'
            onPress={submit}
            isLoading={fetching}
        >
            Add
        </Button>
    </>);
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
                <h3 className='font-semibold'>Workflow statistics</h3>
            </CardHeader>

            <Divider />

            <CardBody>
                <div className='grid grid-cols-2 gap-4 font-bold'>
                    <div>Functional dependencies</div>
                    <div>Evaluated examples</div>
                </div>
                <div className='grid grid-cols-2 gap-4'>
                    <div>Initial: {stats.FDsInitial}</div>
                    <div>Positive: {stats.examplesPositive}</div>
                </div>
                <div className='grid grid-cols-2 gap-4'>
                    <div>Remaining: {stats.FDsRemaining}</div>
                    <div>Negative: {stats.examplesNegative}</div>
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
                <h3 className='font-semibold'>Class statistics</h3>
            </CardHeader>

            <Divider />

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
                        <div className='flex justify-center'>{c.example && <ExampleStateIcon state={c.example.state} />}</div>
                    </div>
                ))}
            </CardBody>
        </Card>
    );
}

type ExampleStateIconProps = {
    state: ExampleState;
    size?: number;
};

export function ExampleStateIcon({ state, size = 24 }: ExampleStateIconProps) {
    const data = exampleStateData[state];

    return (
        <data.icon size={size} className={data.color} />
    );
}

const exampleStateData: {
    [key in ExampleState]: {
        color: string;
        icon: IconType;
    }
} = {
    [ExampleState.New]: { icon: IoReloadCircleOutline, color: 'text-primary' },
    [ExampleState.Rejected]: { icon: IoIosCloseCircleOutline, color: 'text-danger' },
    [ExampleState.Accepted]: { icon: IoIosCheckmarkCircleOutline, color: 'text-success' },
    [ExampleState.Answered]: { icon: IoReloadCircleOutline, color: 'text-primary' },
    [ExampleState.Conflict]: { icon: IoStopCircleOutline, color: 'text-warning' },
};

type RediscoveryFormModalProps = {
    onSubmit: (approach: Approach) => void;
    fetching?: boolean;
};

function RediscoveryFormModal({ onSubmit, fetching }: RediscoveryFormModalProps) {
    const availableApproaches = useApproaches();
    const [ selectedApproach, setSelectedApproach ] = useState(new Set<string>());
    const approachOptions = useMemo(() => availableApproaches?.map(a => nameToOption(a.name)), [ availableApproaches ]);

    function submit() {
        if (!availableApproaches)
            return;

        const selectedApproachName = selectedApproach.values().next().value;
        const approach = availableApproaches.find(a => a.name === selectedApproachName);
        if (!approach)
            return;

        onSubmit(approach);
    }

    if (!approachOptions)
        return null;

    return (
        <ModalContent>
            <ModalHeader>
                Execute rediscovery
            </ModalHeader>

            <ModalBody>
                <Select
                    label='Approach'
                    selectionMode='single'
                    items={approachOptions}
                    selectedKeys={selectedApproach}
                    onSelectionChange={setSelectedApproach as (keys: SharedSelection) => void}
                >
                    {option => (
                        <SelectItem key={option.key}>{option.label}</SelectItem>
                    )}
                </Select>
            </ModalBody>

            <ModalFooter className='justify-start'>
                <Button
                    onPress={submit}
                    isLoading={fetching}
                    isDisabled={!selectedApproach}
                >
                    Execute
                </Button>
            </ModalFooter>
        </ModalContent>
    );
}
