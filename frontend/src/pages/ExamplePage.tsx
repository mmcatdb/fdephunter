import { Page } from '@/components/layout';
import { routes } from '@/router';
import { Workflow } from '@/types/workflow';
import { API } from '@/utils/api';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';

type ExamplePageProps = {
    type: 'workflow' | 'worker' | 'assignment';
};

export function ExamplePage({ type }: ExamplePageProps) {
    const [ isError, setIsError ] = useState(false);
    const navigate = useNavigate();

    function setError() {
        setTimeout(() => setIsError(true), 2000);
    }

    async function findRandomWorkflow(signal?: AbortSignal) {
        const response = await API.workflows.getAll(signal, {});
        if (!response.status || response.data.length === 0) {
            setError();
            return;
        }

        const workflows = response.data.map(Workflow.fromServer);
        navigate(routes.workflow.settings.resolve({ workflowId: workflows[0].id }));
    }

    async function findRandomWorker(signal?: AbortSignal) {
        // const response = await API.workers.getAll(signal, {});
        // if (!response.status || response.data.length === 0) {
        //     setError();
        //     return;
        // }

        // const workers = response.data.map(Worker.fromServer);
        // navigate(routes.worker.detail.resolve({ workerId: workers[0].id }));
        console.log(signal);
        navigate(routes.worker.detail.resolve({ workerId: '456' }));
    }

    async function findRandomAssignment(signal?: AbortSignal) {
        // const response = await API.assignments.getAll(signal, {});
        // if (!response.status || response.data.length === 0) {
        //     setError();
        //     return;
        // }

        // const assignments = response.data.map(Assignment.fromServer);
        // navigate(routes.assignment.detail.resolve({ assignmentId: assignments[0].id }));
        console.log(signal);
        navigate(routes.assignment.root.resolve({ assignmentId: '789' }));
    }

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();

        if (type === 'workflow') {
            void findRandomWorkflow(signal);
            return abort;
        }
        if (type === 'worker') {
            void findRandomWorker(signal);
            return abort;
        }
        if (type === 'assignment') {
            void findRandomAssignment(signal);
            return abort;
        }
    }, [ type ]);

    if (!isError)
        return null;

    return (
        <Page>
            <h1 className='text-lg'>No example of {type} was found :/</h1>
        </Page>
    );
}
