import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { JobResult } from '@/types/jobResult';

export function useJobResult(worklflowId: string): JobResult | undefined {
    const [ jobResult, setJobResult ] = useState<JobResult>();

    const fetchJobResult = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.getLastJobResult(signal, { workflowId: worklflowId });
        if (!response.status)
            return;

        setJobResult(JobResult.fromServer(response.data));
    }, [ worklflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchJobResult(signal);

        return abort;
    }, [ fetchJobResult ]);

    return jobResult;
}
