import { JobState, type Job } from '@/types/job';
import { type IconType } from 'react-icons/lib';
import { IoReloadCircleOutline, IoStopCircleOutline } from 'react-icons/io5';
import { IoIosCloseCircleOutline, IoIosCheckmarkCircleOutline } from 'react-icons/io';
import { displayPercent } from '@/utils/common';
import clsx from 'clsx';
import { Card, CardBody, CardHeader } from '@nextui-org/react';

type JobDisplayProps = {
    job: Job;
};

export function JobDisplay({ job }: JobDisplayProps) {
    const data = jobStateData[job.state];

    return (
        <Card>
            <CardHeader>
                    Job #{job.id}
                <span className={clsx('float-end', data.color)}>
                    {data.icon({ size: 24 })}
                </span>
            </CardHeader>
            <CardBody>
                State: {data.label}<br />
                Started: {job.startDate.toISO()}<br />
                Progress: {displayPercent(job.progress)}
            </CardBody>
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
    [JobState.Waiting]: { color: 'text-warning', icon: IoStopCircleOutline, label: 'Waiting' },
    [JobState.Running]: { color: 'text-info', icon: IoReloadCircleOutline, label: 'Running' },
    [JobState.Pending]: { color: 'text-danger', icon: IoIosCloseCircleOutline, label: 'Pending' },
    [JobState.Finished]: { color: 'text-success', icon: IoIosCheckmarkCircleOutline, label: 'Finished' },
};
