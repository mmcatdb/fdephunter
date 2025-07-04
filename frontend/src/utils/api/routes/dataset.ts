import type { Empty } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { DatasetData, DatasetResponse } from '@/types/dataset';
import { type FileResponse } from '@/types/file';
import { type Id } from '@/types/id';

export const dataset = {
    getDatasets: GET<Empty, DatasetResponse[]>(
        () => `/datasets`,
    ),
    getDatasetData: GET<{ workflowId: Id }, DatasetData, { offset?: number, limit?: number }>(
        u => `/workflows/${u.workflowId}/data`,
    ),
    // FIXME Not implemented yet on the backend!
    uploadDataset: POST<Empty, FileResponse, FormData>(
        () => `/datasets`,
    ),
};
