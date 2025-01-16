import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { type Job } from '@/types/job';
import { type Workflow, WorkflowState } from '@/types/workflow';
import { InitialSettings } from '@/pages/InitialSettings';
import { WaitingForInitialFD } from '@/pages/WaitingForInitialFD';
import { useWorkflow } from '@/hooks';
import { useParams } from 'react-router-dom';
import { type NamedParams, type routes } from '@/router';
import { WorkflowDashboard } from '@/pages/WorkflowDashboard';
import { WaitingForFD } from '@/pages/WaitingForFD';
import { DisplayFinalFDs } from '@/pages/DisplayFinalFDs';
import { Sidebar } from '@/components/layout';

export function WorkflowPage() {
    const { workflowId } = useParams() as NamedParams<typeof routes.workflow.detail>;
    const { workflow, cache, setWorkflow, reload } = useWorkflow(workflowId);

    function runFDJob(newWorkflow: Workflow, job: Job) {
        setWorkflow(newWorkflow, { job });
    }

    if (!workflow)
        return null;

    return (<>
        <Sidebar>
            <WorkflowProgressDisplay currentStep={workflow.state} />
        </Sidebar>

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
            <WorkflowDashboard
                workflow={workflow}
                cachedClasses={cache.classes}
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
