import { useCallback, useEffect, useState } from 'react';
import type { DatasetData } from '@/types/dataset';
import { API } from '@/utils/api';

/**
 * Fetch the data to display on the workflow overview.
 */
export function useWorkflowData(workflowId?: string): DatasetData | undefined {
    const [ data, setData ] = useState<DatasetData>();

    const fetchData = useCallback(async (signal?: AbortSignal) => {
        if (!workflowId)
            return;

        const response = await API.datasets.getDataForWorkflow(signal, { workflowId });
        if (!response.status)
            return;

        setData(response.data);
    }, [ workflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchData(signal);

        return abort;
    }, [ fetchData ]);

    return data;
}
