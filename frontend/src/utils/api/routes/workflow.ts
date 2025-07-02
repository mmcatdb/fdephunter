import type { Empty } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobResponse } from '@/types/job';
import { type WorkflowResponse } from '@/types/workflow';
import { type Id } from '@/types/id';

export const workflow = {
    createWorkflow: POST<Empty, WorkflowResponse>(
        () => `/workflows/create`,
    ),
    getWorkflow: GET<{ workflowId: Id }, WorkflowResponse>(
        u => `/workflows/${u.workflowId}`,
    ),
    startWorkflow: POST<{ workflowId: Id }, CreateJobResponse, StartWorkflowRequest>(
        u => `/workflows/${u.workflowId}/start`,
    ),
    continueWorkflow: POST<{ workflowId: Id }, CreateJobResponse, ContinueWorkflowRequest>(
        u => `/workflows/${u.workflowId}/continue`,
    ),
    acceptAllExamples: POST<{ workflowId: Id }, WorkflowResponse>(
        // TODO
        u => `/workflows/${u.workflowId}/accept-all`,
    ),
    getLastJob: GET<{ workflowId: Id }, JobResponse>(
        u => `/workflows/${u.workflowId}/last-discovery`,
    ),
};

export type StartWorkflowRequest = {
    description: string;
    approach: string;
    datasetId: Id;
};

export type ContinueWorkflowRequest = {
    description: string;
};

export type CreateJobResponse = {
    workflow: WorkflowResponse;
    job: JobResponse;
};
