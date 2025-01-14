import type { Empty, StringLike } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { DatasetData, DatasetFromServer } from '@/types/dataset';

const datasets = {
    getAll: GET<Empty, DatasetFromServer[]>(
        () => `/datasets`,
    ),
    getDataForWorkflow: GET<{ workflowId: StringLike }, DatasetData>(
        u => `/datasets/workflows/${u.workflowId}/data`,
    ),
    getDataForAssignment: GET<{ assignmentId: StringLike }, DatasetData>(
        u => `/datasets/assignments/${u.assignmentId}/data`,
    ),
};

export default datasets;
