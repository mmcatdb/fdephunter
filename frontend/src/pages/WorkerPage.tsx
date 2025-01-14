import AssignmentVerdictLabel from '@/components/AssignmentVerdictLabel';
import SpinnerButton from '@/components/common/SpinnerButton';
import { useAnsweredAssignments, useWorker } from '@/hooks';
import { routes, type NamedParams } from '@/router';
import { WorkerState, type Worker, type WorkerFromServer } from '@/types/worker';
import API from '@/utils/api';
import { useEffect, useState } from 'react';
import { Button, Col, Row, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';
import { Link } from 'react-router-dom';


export default function WorkerPage() {
    const { workerId } = useParams() as NamedParams<typeof routes.worker.detail>;
    const { worker, reload } = useWorker(workerId);

    if (!worker)
        return null;

    return (
        <WorkerReady worker={worker} reload={reload} />
    );
}

type WorkerReadyProps = {
    worker: Worker;
    reload: (nextValue?: WorkerFromServer) => void;
}

function WorkerReady({ worker, reload }: WorkerReadyProps) {
    const navigate = useNavigate();
    const [ fetchingAccept, setFetchingAccept ] = useState(false);
    const [ fetchingReject, setFetchingReject ] = useState(false);
    const answeredAssignments = useAnsweredAssignments(worker.id);
    
    useEffect(() => {
        if (worker.state !== WorkerState.Idle)
            return;

        const interval = setInterval(() => reload(), 2000);

        return () => clearInterval(interval);
    }, [ worker.state, reload ]);

    async function workerAccepted() {
        setFetchingAccept(true);
        const response = await API.workers.accept({ workerId: worker.id });
        setFetchingAccept(false);
        if (!response.status)
            return;

        reload(response.data);
    }

    async function workerRejected() {
        setFetchingReject(true);
        const response = await API.workers.reject({ workerId: worker.id });
        setFetchingReject(false);
        if (!response.status)
            return;

        reload(response.data);
    }

    function goToAssignment() {
        if (worker.assignment)
            navigate(routes.assignment.detail.resolve({ assignmentId: worker.assignment.id }));
    }

    return (
        <div className='text-center'>
            <h1>Hello {worker.user.name}!</h1>
            {worker.state === WorkerState.Pending && (<>
                <p className='mt-5'>
                    You, as a major expert in this domain, have been asked to help us with the evaluation of some negative examples.<br />
                    Will you answer the call?
                </p>
                <div className='mt-5'>
                    <SpinnerButton
                        onClick={workerAccepted}
                        variant='primary'
                        fetching={fetchingAccept}
                        disabled={fetchingReject}
                    >
                        Yes! Of course!
                    </SpinnerButton>
                    <SpinnerButton
                        onClick={workerRejected}
                        variant='danger'
                        className='ms-3'
                        fetching={fetchingReject}
                        disabled={fetchingAccept}
                    >
                        {`No, I can't ...`}
                    </SpinnerButton>
                </div>
            </>)}
            {worker.state === WorkerState.Unsubscribed && (<>
                <p className='mt-5'>
                    Unfortunatelly, you chose not to help us with the evaluation of the negative examples. However, you can always change your mind!
                </p>
                <SpinnerButton
                    onClick={workerAccepted}
                    variant='primary'
                    fetching={fetchingAccept}
                    disabled={fetchingReject}
                >
                    Change my mind
                </SpinnerButton>
            </>)}
            {worker.state === WorkerState.Idle && (<>
                <p className='mt-5'>
                    Thank you for choosing to help us! Please wait a few moments until we generate the next negative example.
                </p>
                <Spinner variant='primary'/>
            </>)}
            {worker.state === WorkerState.Assigned && worker.assignment && (<>
                <p className='mt-5'>
                    Thank you for choosing to help us! Your next assignment is ready:
                </p>
                <Button onClick={goToAssignment}>
                    Go to assignment
                </Button>
            </>)}
            {answeredAssignments && answeredAssignments.length > 0 && (<>
                <h2 className='mt-5'>Previous assignments</h2>
                <Row>
                    <Col />
                    <Col xs='auto' className='text-start'>
                        {answeredAssignments.map(assignment => (
                            <div key={assignment.id} className='d-flex justify-content-between'>
                                <Link to={routes.assignment.detail.resolve({ assignmentId: assignment.id })} className='me-3'>
                                    {routes.assignment.detail.resolve({ assignmentId: assignment.id })}
                                </Link>
                                <span>(<AssignmentVerdictLabel verdict={assignment.verdict} />)</span>
                            </div>
                        ))}
                    </Col>
                    <Col />
                </Row>
            </>)}
        </div>
    );
}
