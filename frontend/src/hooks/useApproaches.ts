import { useEffect, useState } from 'react';
import API from '@/utils/api';
import { Approach } from '@/types/approach';


export function useApproaches(): Approach[] | undefined {
    const [ approaches, setApproaches ] = useState<Approach[]>();

    async function fetchApproaches(signal?: AbortSignal) {
        const response = await API.approaches.getAll(signal, {});
        if (!response.status)
            return;

        setApproaches(response.data.map(Approach.fromServer));
    }

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        fetchApproaches(signal);

        return abort;
    }, []);

    return approaches;
}