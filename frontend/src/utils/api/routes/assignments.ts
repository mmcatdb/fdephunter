import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AssignmentInit, type ExampleDecision, type AssignmentResponse } from '@/types/assignment';
import { type McLattice } from '@/types/armstrongRelation';

export const assignments = {
    create: POST<AssignmentInit, AssignmentResponse, { workflowId: StringLike }>(
        () => `/assignments`,
    ),
    get: GET<{ assignmentId: StringLike }, AssignmentResponse, { limit?: number }>(
        u => `/assignments/${u.assignmentId}`,
    ),
    getAll: GET<{ workflowId: StringLike }, AssignmentResponse[]>(
        u => `/workflows/${u.workflowId}/assignments`,
    ),
    evaluate: POST<{ assignmentId: StringLike }, AssignmentResponse, ExampleDecision>(
        u => `/assignments/${u.assignmentId}/evaluate`,
    ),
    reset: POST<{ assignmentId: StringLike }, AssignmentResponse>(
        u => `/assignments/${u.assignmentId}/reset`,
    ),
    getLattices: POST<{ workflowId: StringLike }, McLattice[]>(
        u => `/workflows/${u.workflowId}/lattices`,
    ),
};
