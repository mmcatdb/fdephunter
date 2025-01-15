import { AssignmentVerdictLabel } from '@/components/AssignmentVerdictLabel';
import { Page } from '@/components/layout';
import { useAnsweredAssignments, useWorker } from '@/hooks';
import { routes, type NamedParams } from '@/router';
import { WorkerState, type Worker, type WorkerFromServer } from '@/types/worker';
import { API } from '@/utils/api';
import { Button, Spinner } from '@nextui-org/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

export function WorkerPage() {
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
};

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
        <Page className='space-y-10 text-center'>
            <h1 className='text-lg'>Hello {worker.user.name}!</h1>

            {worker.state === WorkerState.Pending && (<>
                <p>
                    You, as a major expert in this domain, have been asked to help us with the evaluation of some negative examples.<br />
                    Will you answer the call?
                </p>

                <div>
                    <Button
                        onPress={workerAccepted}
                        color='primary'
                        isLoading={fetchingAccept}
                        isDisabled={fetchingReject}
                    >
                        Yes! Of course!
                    </Button>
                    <Button
                        onPress={workerRejected}
                        color='danger'
                        className='ml-4'
                        isLoading={fetchingReject}
                        isDisabled={fetchingAccept}
                    >
                        {`No, I can't ...`}
                    </Button>
                </div>
            </>)}

            {worker.state === WorkerState.Unsubscribed && (<>
                <p>
                    Unfortunatelly, you chose not to help us with the evaluation of the negative examples. However, you can always change your mind!
                </p>

                <Button
                    onPress={workerAccepted}
                    color='primary'
                    isLoading={fetchingAccept}
                    isDisabled={fetchingReject}
                >
                    Change my mind
                </Button>
            </>)}

            {worker.state === WorkerState.Idle && (<>
                <p>
                    Thank you for choosing to help us! Please wait a few moments until we generate the next negative example.
                </p>

                <Spinner color='primary'/>
            </>)}

            {worker.state === WorkerState.Assigned && worker.assignment && (<>
                <p>
                    Thank you for choosing to help us! Your next assignment is ready:
                </p>

                <Button color='primary' onPress={goToAssignment}>
                    Go to assignment
                </Button>
            </>)}

            {answeredAssignments && answeredAssignments.length > 0 && (<>
                <h2>Previous assignments</h2>

                <div className='flex justify-center'>
                    <div className='text-start'>
                        {answeredAssignments.map(assignment => (
                            <div key={assignment.id} className='flex justify-between'>
                                <Link to={routes.assignment.detail.resolve({ assignmentId: assignment.id })} className='mr-4'>
                                    {routes.assignment.detail.resolve({ assignmentId: assignment.id })}
                                </Link>
                                <span>(<AssignmentVerdictLabel verdict={assignment.verdict} />)</span>
                            </div>
                        ))}
                    </div>
                </div>
            </>)}
        </Page>
    );
}
