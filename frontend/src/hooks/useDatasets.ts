import { useEffect, useState } from 'react';
import { Dataset } from '@/types/dataset';
import { API } from '@/utils/api';


export function useDatasets(): Dataset[] | undefined {
    const [ datasets, setDatasets ] = useState<Dataset[]>();

    async function fetchDatasets(signal?: AbortSignal) {
        const response = await API.datasets.getAll(signal, {});
        if (!response.status)
            return;

        setDatasets(response.data.map(Dataset.fromServer));
    }

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchDatasets(signal);

        return abort;
    }, []);

    return datasets;
}
