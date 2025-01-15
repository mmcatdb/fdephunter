import { WorkflowState } from '@/types/workflow';
import { getStringEnumValues } from '@/utils/common';
import clsx from 'clsx';

type WorkflowProgressDisplayProps = {
    currentStep: WorkflowState;
};

export function WorkflowProgressDisplay({ currentStep }: WorkflowProgressDisplayProps) {
    const currentStepIndex = STEPS.findIndex(step => step === currentStep);

    return (
        <div className='h-full py-8 flex flex-col items-center'>
            {STEPS.map((step, index) => (
                <WorkflowStepDisplay
                    key={index}
                    step={step}
                    isActive={index === currentStepIndex}
                    isFinished={index < currentStepIndex}
                    isLast={index === STEPS.length - 1}
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
    isLast: boolean;
};

function WorkflowStepDisplay({ step, isFinished, isActive, isLast }: WorkflowStepDisplayProps) {
    return (<>
        <div className={clsx('size-10 rounded-full flex items-center justify-center text-3xl font-semibold text-white bg-default-400 transition-colors duration-1000',
            isActive && 'bg-primary',
            isFinished && 'bg-success',
        )}>
            {STEP_LABELS[step].shortLabel}
        </div>

        <div className={clsx('transition-colors duration-1000', isActive && 'text-primary', isFinished && 'text-success')}>
            {STEP_LABELS[step].longLabel}
        </div>

        {!isLast && (
            <div className={clsx('grow my-2 w-3 rounded-full bg-default-400 transition-colors duration-1000', isFinished && 'bg-success')} />
        )}
    </>);
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
