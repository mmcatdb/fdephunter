import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type ExampleDecision, type AssignmentResponse } from '@/types/assignment';

export const assignment = {
    getAssignment: GET<{ assignmentId: StringLike }, AssignmentResponse>(
        u => `/assignments/${u.assignmentId}`,
    ),
    getAssignments: GET<{ workflowId: StringLike }, AssignmentResponse[]>(
        u => `/workflows/${u.workflowId}/assignments`,
    ),
    evaluateAssignment: POST<{ assignmentId: StringLike }, AssignmentResponse, ExampleDecision>(
        u => `/assignments/${u.assignmentId}/evaluate`,
    ),
    resetAssignment: POST<{ assignmentId: StringLike }, AssignmentResponse>(
        u => `/assignments/${u.assignmentId}/reset`,
    ),
};
