import { JobState, type Job } from '@/types/job';
import { type IconType } from 'react-icons/lib';
import { IoReloadCircleOutline, IoStopCircleOutline } from 'react-icons/io5';
import { IoIosCloseCircleOutline, IoIosCheckmarkCircleOutline } from 'react-icons/io';
import { Card } from 'react-bootstrap';
import { displayPercent } from '@/utils/common';

type JobDisplayProps = {
    job: Job;
};

export default function JobDisplay({ job }: JobDisplayProps) {
    const data = jobStateData[job.state];

    return (
        <Card>
            <Card.Header>
                <Card.Title>
                    Job #{job.id}
                    <span className={`float-end text-${data.color}`}>
                        {data.icon({ size: 24 })}
                    </span>
                </Card.Title>
            </Card.Header>
            <Card.Body>
                State: {data.label}<br />
                Started: {job.startDate.toISO()}<br />
                Progress: {displayPercent(job.progress)}
            </Card.Body>
        </Card>
    );
}

const jobStateData: {
    [key in JobState]: {
        color: string;
        icon: IconType;
        label: string;
    }
} = {
    [JobState.Waiting]: { color: 'warning', icon: IoStopCircleOutline, label: 'Waiting' },
    [JobState.Running]: { color: 'info', icon: IoReloadCircleOutline, label: 'Running' },
    [JobState.Pending]: { color: 'danger', icon: IoIosCloseCircleOutline, label: 'Pending' },
    [JobState.Finished]: { color: 'success', icon: IoIosCheckmarkCircleOutline, label: 'Finished' },
};