import { WorkflowState } from '@/types/workflow';
import { getStringEnumValues } from '@/utils/common';
import clsx from 'clsx';

type WorkflowProgressDisplayProps = {
    currentStep: WorkflowState;
};

export function WorkflowProgressDisplay({ currentStep }: WorkflowProgressDisplayProps) {
    const currentStepIndex = STEPS.findIndex(step => step === currentStep);

    return (
        <div className='fd-workflow-display'>
            {STEPS.map((step, index) => (
                <WorkflowStepDisplay
                    key={index}
                    step={step}
                    isActive={index === currentStepIndex}
                    isFinished={index < currentStepIndex}
                />
            ))}

        </div>
    );
}

const STEPS = getStringEnumValues(WorkflowState);

type WorkflowStepDisplayProps = {
    step: WorkflowState;
    isFinished: boolean;
    isActive: boolean;
};

function WorkflowStepDisplay({ step, isFinished, isActive }: WorkflowStepDisplayProps) {
    return (
        <div className={clsx('fd-workflow-step', isFinished && 'finished', isActive && 'active')}>
            <div className='fd-workflow-step-body'>
                <div>
                    {STEP_LABELS[step].shortLabel}
                </div>
            </div>
            <div className='fd-workflow-step-label'>
                {STEP_LABELS[step].longLabel}
            </div>
            <div className='fd-workflow-step-between'>
                <div className='fd-workflow-step-bar' />
            </div>
        </div>
    );
}

type StepLabel = {
    shortLabel: string;
    longLabel: string;
};

const STEP_LABELS: { [key in WorkflowState]: StepLabel } = {
    [WorkflowState.InitialSettings]:         { shortLabel: '1', longLabel: 'Initial settings' },
    [WorkflowState.WaitingForInitialFD]:     { shortLabel: '2', longLabel: 'FD discovery' },
    [WorkflowState.DisplayFD]:               { shortLabel: '3', longLabel: 'FD display' },
    //[WorkflowState.WaitingForNextSample]:    { shortLabel: '4', longLabel: 'Generating next sample' },
    //[WorkflowState.EvaluatingSample]:        { shortLabel: '5', longLabel: 'Sample evaluation' },
    [WorkflowState.WaitingForFD]:            { shortLabel: '4', longLabel: 'FD rediscovery' },
    [WorkflowState.DisplayFinalFD]:          { shortLabel: '5', longLabel: 'Final FD display' },
};
