import { routes, type NamedParams } from '@/router';
import { type User } from '@/types/user';
import { Worker } from '@/types/worker';
import { type WorkflowStats, type Workflow, type Class } from '@/types/workflow';
import API from '@/utils/api';
import { useCallback, useMemo, useState } from 'react';
import { Card, Col, Form, Row } from 'react-bootstrap';
import { IoIosCheckmarkCircleOutline, IoIosCloseCircleOutline } from 'react-icons/io';
import { useParams } from 'react-router';
import { FormSelect } from './common/FormSelect';
import { useUsers, useWorkers } from '@/hooks';
import { type SingleValue } from 'react-select';
import { Link } from 'react-router-dom';
import { NegativeExampleState } from '@/types/negativeExample';
import { type IconType } from 'react-icons/lib';
import { IoReloadCircleOutline, IoStopCircleOutline } from 'react-icons/io5';
import SpinnerButton from './common/SpinnerButton';

type WorkersDistributionProps = {
    workflow: Workflow;
    classes: Class[];
};

export default function WorkersDistribution({ workflow, classes }: WorkersDistributionProps) {
    return (
        <div>
            <Row>
                <Col>
                    <WorkflowStatsCard stats={MOCK_WORKFLOW_STATS} />
                    <div className='mt-3'>
                        <WorkersOverviewCard />
                    </div>
                </Col>
                <Col>
                    <ClassStatsCard classes={classes} />
                </Col>
            </Row>
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
            <Card.Header>
                <Card.Title>Workers</Card.Title>
            </Card.Header>
            <Card.Body>
                <WorkersTable workers={workers} />
            </Card.Body>
            <Card.Footer>
                <AddWorker onCreated={workerCreated} />
            </Card.Footer>
        </Card>
    );
}

type WorkersTableProps = {
    workers: Worker[];
}

function WorkersTable({ workers }: WorkersTableProps) {
    return (<>
        <Row className='fw-bold'>
            <Col>User</Col>
            <Col className='text-center'>Link</Col>
        </Row>
        {workers.map(worker => (
            <Row key={worker.id}>
                <Col>{worker.user.name}</Col>
                <Col className='text-center'>
                    <Link to={routes.worker.detail.resolve({ workerId: worker.id })} target='_blank' rel='noreferrer'>
                        {routes.worker.detail.resolve({ workerId: worker.id })}
                    </Link>
                </Col>
            </Row>
        ))}
    </>);
}

type Option = {
    label: string;
    value: string;
}

function userToOption(user: User): Option {
    return {
        label: user.name,
        value: user.id,
    };
}

type AddWorkerProps = {
    onCreated: (worker: Worker) => void;
};

function AddWorker({ onCreated }: AddWorkerProps) {
    const users = useUsers();
    const [ selectedUser, setSelectedUser ] = useState<Option>();
    const userOptions = useMemo(() => users?.map(userToOption), [ users ]);
    const [ fetching, setFetching ] = useState(false);

    const handleUserChange = useCallback((option: SingleValue<Option>) => {
        setSelectedUser(option ?? undefined);
    }, []);
    
    const { workflowId } = useParams() as NamedParams<typeof routes.workflow.detail>;

    async function submit() {
        const user = users?.find(u => u.id === selectedUser?.value);
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
        setSelectedUser(undefined);
    }

    return (<>
        <Row className='pb-1'>
            <Form.Group as={Col}>
                <Form.Label>Add another user</Form.Label>
                <FormSelect
                    options={userOptions}
                    value={selectedUser}
                    onChange={handleUserChange}
                />
            </Form.Group>
            <Col xs={4} className='d-flex align-items-end'>
                <SpinnerButton
                    className='w-100'
                    onClick={submit}
                    fetching={fetching}
                >
                    Add
                </SpinnerButton>
            </Col>
        </Row>
    </>);
}

type WorkflowStatsCardProps = {
    stats: WorkflowStats;
};

function WorkflowStatsCard({ stats }: WorkflowStatsCardProps) {
    return (
        <Card>
            <Card.Header>
                <Card.Title>
                    Workflow statistics
                </Card.Title>
            </Card.Header>
            <Card.Body>
                <Row className='fw-bold'>
                    <Col>Functional dependencies</Col>
                    <Col>Evaluated examples</Col>
                </Row>
                <Row>
                    <Col>Initial: {stats.FDsInitial}</Col>
                    <Col>Positive: {stats.examplesPositive}</Col>
                </Row>
                <Row>
                    <Col>Remaining: {stats.FDsRemaining}</Col>
                    <Col>Negative: {stats.examplesNegative}</Col>
                </Row>
            </Card.Body>
        </Card>
    );
}

type ClassStatsCardProps = {
    classes: Class[];
}

function ClassStatsCard({ classes }: ClassStatsCardProps) {
    console.log(classes);

    return (
        <Card>
            <Card.Header>
                <Card.Title>
                    Class statistics
                </Card.Title>
            </Card.Header>
            <Card.Body>
                <Row className='fw-bold'>
                    <Col>Name</Col>
                    <Col xs={2} className='text-center'>Weight</Col>
                    <Col xs={2} className='text-center'>Iteration</Col>
                    <Col xs={2} className='text-center'>State</Col>
                </Row>
                {classes.map(c => (
                    <Row key={c.id}>
                        <Col>{c.label}</Col>
                        <Col xs={2} className='text-center'>{c.weight}</Col>
                        <Col xs={2} className='text-center'>{c.iteration}</Col>
                        <Col xs={2} className='text-center'>{c.example && <ExampleStateIcon state={c.example.state} />}</Col>
                    </Row>
                ))}
            </Card.Body>
        </Card>
    );
}

type ExampleStateIconProps = {
    state: NegativeExampleState;
}

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
    [NegativeExampleState.New]: { icon: IoReloadCircleOutline, color: 'info' },
    [NegativeExampleState.Rejected]: { icon: IoIosCloseCircleOutline, color: 'danger' },
    [NegativeExampleState.Accepted]: { icon: IoIosCheckmarkCircleOutline, color: 'success' },
    [NegativeExampleState.Answered]: { icon: IoReloadCircleOutline, color: 'info' },
    [NegativeExampleState.Conflict]: { icon: IoStopCircleOutline, color: 'warning' },
};