import { WorkflowState } from '@/types/workflow';
import { getStringEnumValues } from '@/utils/common';
import clsx from 'clsx';

type WorkflowProgressDisplayProps = {
    currentStep: WorkflowState;
};

export function WorkflowProgressDisplay({ currentStep }: WorkflowProgressDisplayProps) {
    const currentStepIndex = STEPS.findIndex(step => step === currentStep);

    return (
        <div className='h-full py-8 flex flex-col items-center gap-3'>
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
        <div className='flex flex-col items-center [@media_(max-height:500px)]:flex-row'>
            <div className={clsx('size-10 rounded-full flex items-center justify-center text-3xl font-semibold text-white bg-default-400 transition-colors duration-1000',
                isActive && 'bg-primary',
                isFinished && 'bg-success',
            )}>
                {STEP_LABELS[step].shortLabel}
            </div>

            <div className={clsx('w-40 text-center transition-colors duration-1000', isActive && 'text-primary', isFinished && 'text-success')}>
                {STEP_LABELS[step].longLabel}
            </div>
        </div>

        {!isLast && (
            <div className={clsx('grow w-2 rounded-full bg-default-400 transition-colors duration-1000 [@media_(max-height:500px)]:hidden', isFinished && 'bg-success')} />
        )}
    </>);
}

type StepLabel = {
    shortLabel: string;
    longLabel: string;
};

const STEP_LABELS: { [key in WorkflowState]: StepLabel } = {
    [WorkflowState.InitialSettings]:        { shortLabel: '1', longLabel: 'Input dataset' },
    [WorkflowState.InitialFdDiscovery]:    { shortLabel: '2', longLabel: 'FD discovery' },
    [WorkflowState.NegativeExamples]:       { shortLabel: '3', longLabel: 'Negative examples' },
    [WorkflowState.PositiveExamples]:       { shortLabel: '4', longLabel: 'Positive examples' },
    [WorkflowState.DisplayFinalFds]:         { shortLabel: '5', longLabel: 'Final FD display' },
};
