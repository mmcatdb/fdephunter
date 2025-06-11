import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { DatasetData, DatasetResponse } from '@/types/dataset';
import { type FileResponse } from '@/types/file';

export const datasets = {
    getAll: GET<Empty, DatasetResponse[]>(
        () => `/datasets`,
    ),
    getDataForWorkflow: GET<{ workflowId: StringLike }, DatasetData>(
        u => `/datasets/workflows/${u.workflowId}/data`,
    ),
    getDataForAssignment: GET<{ assignmentId: StringLike }, DatasetData>(
        u => `/datasets/assignments/${u.assignmentId}/data`,
    ),
    // FIXME Not implemented yet on the backend!
    uploadFile: POST<Empty, FileResponse, FormData>(
        () => `/files`,
    ),
};
