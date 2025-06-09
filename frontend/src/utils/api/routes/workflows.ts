import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { ExecuteDiscoveryParams, ExecuteRediscoveryParams, JobFromServer } from '@/types/job';
import { type WorkflowFromServer, type ClassFromServer } from '@/types/workflow';
import { type JobResultFromServer } from '@/types/jobResult';

export type CreateJobResponse = {
    workflow: WorkflowFromServer;
    job: JobFromServer;
};

export const workflows = {
    create: POST<Empty, WorkflowFromServer, Empty>(
        () => `/workflows/create`,
    ),
    get: GET<{ workflowId: StringLike }, WorkflowFromServer>(
        u => `/workflows/${u.workflowId}`,
    ),
    getAll: GET<Empty, WorkflowFromServer[]>(
        () => `/workflows`,
    ),
    executeDiscovery: POST<{ workflowId: StringLike }, CreateJobResponse, ExecuteDiscoveryParams>(
        u => `/workflows/${u.workflowId}/execute-discovery`,
    ),
    executeRediscovery: POST<{ workflowId: StringLike }, CreateJobResponse, ExecuteRediscoveryParams>(
        u => `/workflows/${u.workflowId}/execute-rediscovery`,
    ),
    getLastJob: GET<{ workflowId: StringLike }, JobFromServer>(
        u => `/workflows/${u.workflowId}/last-discovery`,
    ),
    getLastJobResult: GET<{ workflowId: StringLike }, JobResultFromServer>(
        u => `/workflows/${u.workflowId}/last-result`,
    ),
    getClasses: GET<{ workflowId: StringLike }, ClassFromServer[]>(
        u => `/workflows/${u.workflowId}/classes`,
    ),
};
