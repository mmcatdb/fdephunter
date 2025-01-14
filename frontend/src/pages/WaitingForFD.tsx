import JobDisplay from '@/components/job/JobDisplay';
import { useJob } from '@/hooks';
import { type Job, JobState } from '@/types/job';
import { Button, Col, Row } from 'react-bootstrap';

type WaitingForFDProps = {
    workflowId: string;
    cachedJob?: Job;
    onNextStep: () => void;
}

export default function WaitingForFD({ workflowId, cachedJob, onNextStep }: WaitingForFDProps) {
    const job = useJob(workflowId, cachedJob);

    if (!job)
        return null;

    return (<>
        <h1>Wait for FD discovery ...</h1>
        <Row className='mt-5'>
            <Col xxl={6} xl={8}>
                <JobDisplay job={job} />
            </Col>
        </Row>
        <div className='mt-5'>
            <Button onClick={onNextStep} disabled={!job || job.state !== JobState.Finished}>
                Go next
            </Button>
        </div>
    </>);
}