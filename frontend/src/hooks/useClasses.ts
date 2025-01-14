import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { Class } from '@/types/workflow';

const REFRESH_TIMEOUT = 2000; // in ms

export function useClasses(workflowId: string, initialClasses?: Class[]): Class[] {
    const [ classes, setClasses ] = useState(initialClasses ?? []);

    const refreshClasses = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.getClasses(signal, { workflowId });
        if (!response.status)
            return;

        setClasses(response.data.map(Class.fromServer));

    }, [ workflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        const interval = setInterval(() => {
            void refreshClasses(signal);
        }, REFRESH_TIMEOUT);

        if (!initialClasses)
            void refreshClasses(signal);

        return () => {
            clearInterval(interval);
            abort();
        };
    }, [ initialClasses, refreshClasses ]);

    return classes;
}
