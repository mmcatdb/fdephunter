import { JobDisplay } from '@/components/job/JobDisplay';
import { Page } from '@/components/layout';
import { useJob } from '@/hooks';
import { type Job, JobState } from '@/types/job';
import { Button } from '@nextui-org/react';

type WaitingForInitialFDProps = {
    workflowId: string;
    cachedJob?: Job;
    onNextStep: () => void;
};

export function WaitingForInitialFD({ workflowId, cachedJob, onNextStep }: WaitingForInitialFDProps) {
    const job = useJob(workflowId, cachedJob);

    if (!job)
        return null;

    return (
        <Page className='max-w-xl space-y-10'>
            <h1 className='text-lg'>Wait for initial FD discovery ...</h1>

            <JobDisplay job={job} />

            <div className='flex justify-end'>
                <Button color='primary' onPress={onNextStep} isDisabled={job.state !== JobState.Finished}>
                    Go next
                </Button>
            </div>
        </Page>
    );
}
