import { AssignmentVerdictLabel } from '@/components/AssignmentVerdictLabel';
import { Page } from '@/components/layout';
import { routes } from '@/router';
import { type AssignmentInfo } from '@/types/assignment';
import { Worker, WorkerState } from '@/types/worker';
import { API } from '@/utils/api';
import { Button, Spinner } from '@nextui-org/react';
import { useEffect, useState } from 'react';
import { Link, type Params, useLoaderData, useNavigate, useRevalidator } from 'react-router';

const REFRESH_TIMEOUT = 2000; // in ms

export function WorkerPage() {
    const { worker, answeredAssignments } = useLoaderData<WorkerLoaded>();
    const revalidator = useRevalidator();

    const navigate = useNavigate();
    const [ fetchingAccept, setFetchingAccept ] = useState(false);
    const [ fetchingReject, setFetchingReject ] = useState(false);

    useEffect(() => {
        if (worker.state !== WorkerState.Idle)
            return;

        const interval = setInterval(() => {
            void revalidator.revalidate();
        }, REFRESH_TIMEOUT);

        return () => clearInterval(interval);
    }, [ worker.state, revalidator ]);

    async function workerAccepted() {
        setFetchingAccept(true);
        const response = await API.workers.accept({ workerId: worker.id });
        setFetchingAccept(false);
        if (!response.status)
            return;

        await revalidator.revalidate();
    }

    async function workerRejected() {
        setFetchingReject(true);
        const response = await API.workers.reject({ workerId: worker.id });
        setFetchingReject(false);
        if (!response.status)
            return;

        await revalidator.revalidate();
    }

    function goToAssignment() {
        if (worker.assignment)
            void navigate(routes.assignment.root.resolve({ assignmentId: worker.assignment.id }));
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
                    Thank you for choosing to help us!<br />
                    Please wait a few moments until we generate the next negative example.
                </p>

                <Spinner color='primary'/>
            </>)}

            {worker.state === WorkerState.Assigned && worker.assignment && (<>
                <p>
                    Thank you for choosing to help us!<br />
                    Your next assignment is ready:
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
                                <Link to={routes.assignment.root.resolve({ assignmentId: assignment.id })} className='mr-4'>
                                    {routes.assignment.root.resolve({ assignmentId: assignment.id })}
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

type WorkerLoaded = {
    worker: Worker;
    answeredAssignments: AssignmentInfo[];
};

WorkerPage.loader = async ({ params: { workerId } }: { params: Params<'workerId'> }): Promise<WorkerLoaded> => {
    if (!workerId)
        throw new Error('Missing worker ID');

    const [ workerResponse, assignmentsResponse ] = await Promise.all([
        API.workers.get(undefined, { workerId }),
        API.assignments.getAllAnswered(undefined, { workerId }),
    ]);
    if (!workerResponse.status)
        throw new Error('Failed to load worker');
    if (!assignmentsResponse.status)
        throw new Error('Failed to load answered assignments');

    return {
        worker: Worker.fromServer(workerResponse.data),
        answeredAssignments: assignmentsResponse.data,
    };
};
