import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { type Job } from '@/types/job';
import { type Workflow, WorkflowState } from '@/types/workflow';
import { createPortal } from 'react-dom';
import InitialSettings from '@/pages/InitialSettings';
import WaitingForInitialFD from './WaitingForInitialFD';
import { useWorkflow } from '@/hooks';
import { useParams } from 'react-router-dom';
import { type NamedParams, type routes } from '@/router';
import DisplayFDs from './DisplayFDs';
import WaitingForFD from './WaitingForFD';
import DisplayFinalFDs from './DisplayFinalFDs';

export default function WorkflowPage() {
    const { workflowId } = useParams() as NamedParams<typeof routes.workflow.detail>;
    const { workflow, cache, setWorkflow, reload } = useWorkflow(workflowId);

    function runFDJob(newWorkflow: Workflow, job: Job) {
        setWorkflow(newWorkflow, { job });
    }

    if (!workflow)
        return null;

    const leftBar = document.getElementById('left-bar');

    return (<>
        {leftBar && createPortal(
            <WorkflowProgressDisplay currentStep={workflow.state} />,
            leftBar,
        )}
        {workflow.state === WorkflowState.InitialSettings && (
            <InitialSettings workflow={workflow} onNextStep={runFDJob} />
        )}
        {workflow.state === WorkflowState.WaitingForInitialFD && (
            <WaitingForInitialFD
                workflowId={workflow.id}
                cachedJob={cache.job}
                onNextStep={reload}
            />
        )}
        {workflow.state === WorkflowState.DisplayFD && (
            <DisplayFDs
                workflow={workflow}
                chachedClasses={cache.classes}
                onNextStep={runFDJob}
            />
        )}
        {workflow.state === WorkflowState.WaitingForFD && (
            <WaitingForFD
                workflowId={workflow.id}
                cachedJob={cache.job}
                onNextStep={reload}
            />
        )}
        {workflow.state === WorkflowState.DisplayFinalFD && (<>
            <DisplayFinalFDs workflow={workflow} />
        </>)}
    </>);
}
