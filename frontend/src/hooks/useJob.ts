import { useCallback, useEffect, useState } from 'react';
import API from '@/utils/api';
import { Job } from '@/types/job';

const REFRESH_TIMEOUT = 2000; // in ms

export function useJob(workflowId: string, initialJob?: Job): Job | undefined {
    const [ job, setJob ] = useState(initialJob);

    const refreshJob = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.getLastJob(signal, { workflowId });
        if (!response.status) 
            return;

        setJob(Job.fromServer(response.data));

    }, [ workflowId ]);
    
    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        const interval = setInterval(() => {
            refreshJob(signal);
        }, REFRESH_TIMEOUT);

        if (!initialJob)
            refreshJob(signal);
        
        return () => {
            clearInterval(interval);
            abort();
        };
    }, [ initialJob, refreshJob ]);

    return job;
}