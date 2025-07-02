import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobResponse, JobResultResponse } from '@/types/job';
import { type WorkflowResponse } from '@/types/workflow';

export const workflow = {
    createWorkflow: POST<Empty, WorkflowResponse, Empty>(
        () => `/workflows/create`,
    ),
    getWorkflow: GET<{ workflowId: StringLike }, WorkflowResponse>(
        u => `/workflows/${u.workflowId}`,
    ),
    startWorkflow: POST<{ workflowId: StringLike }, CreateJobResponse, StartWorkflowRequest>(
        u => `/workflows/${u.workflowId}/start`,
    ),
    continueWorkflow: POST<{ workflowId: StringLike }, CreateJobResponse, ContinueWorkflowRequest>(
        u => `/workflows/${u.workflowId}/continue`,
    ),
    acceptAllExamples: POST<{ workflowId: StringLike }, WorkflowResponse>(
        // TODO
        u => `/workflows/${u.workflowId}/accept-all`,
    ),
    getLastJob: GET<{ workflowId: StringLike }, JobResponse>(
        u => `/workflows/${u.workflowId}/last-discovery`,
    ),
    getLastJobResult: GET<{ workflowId: StringLike }, JobResultResponse>(
        u => `/workflows/${u.workflowId}/last-result`,
    ),
};

export type StartWorkflowRequest = {
    description: string;
    approach: string;
    datasetName: string;
};

export type ContinueWorkflowRequest = {
    description: string;
};

export type CreateJobResponse = {
    workflow: WorkflowResponse;
    job: JobResponse;
};
