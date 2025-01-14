import { JobDisplay } from '@/components/job/JobDisplay';
import { useJob } from '@/hooks';
import { type Job, JobState } from '@/types/job';
import { Button } from '@nextui-org/react';

type WaitingForInitialFDProps = {
    workflowId: string;
    cachedJob?: Job;
    onNextStep: () => void;
};

export default function WaitingForInitialFD({ workflowId, cachedJob, onNextStep }: WaitingForInitialFDProps) {
    const job = useJob(workflowId, cachedJob);

    if (!job)
        return null;

    return (<>
        <h1>Wait for initial FD discovery ...</h1>

        <div className='mt-12 lg:w-2/3 xl:w-1/2'>
            <JobDisplay job={job} />
        </div>

        <div className='mt-12'>
            <Button onPress={onNextStep} disabled={job.state !== JobState.Finished}>
                Go next
            </Button>
        </div>
    </>);
}
