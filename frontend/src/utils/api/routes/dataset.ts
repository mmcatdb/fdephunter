import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { DatasetData, DatasetResponse } from '@/types/dataset';
import { type FileResponse } from '@/types/file';

export const dataset = {
    getDatasets: GET<Empty, DatasetResponse[]>(
        () => `/datasets`,
    ),
    getDatasetData: GET<{ workflowId: StringLike }, DatasetData, { limit?: number }>(
        u => `/workflows/${u.workflowId}/data`,
    ),
    // FIXME Not implemented yet on the backend!
    uploadDataset: POST<Empty, FileResponse, FormData>(
        () => `/datasets`,
    ),
};
