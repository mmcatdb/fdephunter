import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { Class } from '@/types/workflow';

const REFRESH_TIMEOUT = 2000; // in ms

export function useClasses(workflowId: string, initialClasses?: Class[], refreshTimeout: number | null = REFRESH_TIMEOUT): Class[] {
    const [ classes, setClasses ] = useState(initialClasses ?? []);

    const refreshClasses = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.getClasses(signal, { workflowId });
        if (!response.status)
            return;

        setClasses(response.data.map(Class.fromServer));

    }, [ workflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        if (!initialClasses)
            void refreshClasses(signal);

        if (!refreshTimeout)
            return () => abort();

        const interval = setInterval(() => {
            void refreshClasses(signal);
        }, refreshTimeout);

        return () => {
            clearInterval(interval);
            abort();
        };
    }, [ initialClasses, refreshClasses ]);

    return classes;
}
