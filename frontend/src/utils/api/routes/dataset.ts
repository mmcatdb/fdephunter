import type { Empty } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { DatasetData, DatasetResponse } from '@/types/dataset';
import { type Id } from '@/types/id';

export type FileResponse = {
    hash: string;
    originalName: string;
};

export const dataset = {
    getDatasets: GET<Empty, DatasetResponse[]>(
        () => `/datasets`,
    ),
    getDatasetData: GET<{ workflowId: Id }, DatasetData, { offset?: number, limit?: number }>(
        u => `/workflows/${u.workflowId}/data`,
    ),
    uploadFile: POST<Empty, FileResponse, FormData>(
        () => `/files`,
    ),
};
