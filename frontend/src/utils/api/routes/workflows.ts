import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { ExecuteDiscoveryParams, ExecuteRediscoveryParams, JobResponse, JobResultResponse } from '@/types/job';
import { type WorkflowResponse } from '@/types/workflow';

export type CreateJobResponse = {
    workflow: WorkflowResponse;
    job: JobResponse;
};

export const workflows = {
    create: POST<Empty, WorkflowResponse, Empty>(
        () => `/workflows/create`,
    ),
    get: GET<{ workflowId: StringLike }, WorkflowResponse>(
        u => `/workflows/${u.workflowId}`,
    ),
    executeDiscovery: POST<{ workflowId: StringLike }, CreateJobResponse, ExecuteDiscoveryParams>(
        u => `/workflows/${u.workflowId}/execute-discovery`,
    ),
    executeRediscovery: POST<{ workflowId: StringLike }, CreateJobResponse, ExecuteRediscoveryParams>(
        u => `/workflows/${u.workflowId}/execute-rediscovery`,
    ),
    getLastJob: GET<{ workflowId: StringLike }, JobResponse>(
        u => `/workflows/${u.workflowId}/last-discovery`,
    ),
    getLastJobResult: GET<{ workflowId: StringLike }, JobResultResponse>(
        u => `/workflows/${u.workflowId}/last-result`,
    ),
};
