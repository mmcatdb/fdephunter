import { type Dispatch, type SetStateAction, useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { Worker, type WorkerFromServer } from '@/types/worker';

type UseWorkerReturn = {
    worker?: Worker;
    reload: (nextValue?: WorkerFromServer) => void;
};

export function useWorker(workerId: string): UseWorkerReturn {
    const [ worker, setWorker ] = useState<Worker>();

    const fetchWorker = useCallback(async (nextValue?: WorkerFromServer, signal?: AbortSignal) => {
        if (nextValue) {
            setWorker(Worker.fromServer(nextValue));
            return;
        }

        const response = await API.workers.get(signal, { workerId });
        if (!response.status)
            return;

        setWorker(Worker.fromServer(response.data));
    }, [ workerId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchWorker(undefined, signal);

        return abort;
    }, [ fetchWorker ]);

    return {
        worker,
        reload: fetchWorker,
    };
}

type UseWorkersReturn = {
    workers: Worker[];
    setWorkers: Dispatch<SetStateAction<Worker[]>>;
};

export function useWorkers(workflowId: string): UseWorkersReturn {
    const [ workers, setWorkers ] = useState<Worker[]>([]);

    const fetchWorkers = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.getAllWorkers(signal, { workflowId });
        if (!response.status)
            return;

        setWorkers(response.data.map(Worker.fromServer));
    }, [ workflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchWorkers(signal);

        return abort;
    }, [ fetchWorkers ]);

    return {
        workers,
        setWorkers,
    };
}
