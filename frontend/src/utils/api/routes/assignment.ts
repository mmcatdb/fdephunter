import { type ExampleDecision } from '@/types/armstrongRelation';
import { GET, POST } from '../routeFunctions';
import { type AssignmentResponse } from '@/types/assignment';
import { type Id } from '@/types/id';

export const assignment = {
    getAssignment: GET<{ assignmentId: Id }, AssignmentResponse>(
        u => `/assignments/${u.assignmentId}`,
    ),
    getAssignments: GET<{ workflowId: Id }, AssignmentResponse[]>(
        u => `/workflows/${u.workflowId}/assignments`,
    ),
    evaluateAssignment: POST<{ assignmentId: Id }, AssignmentResponse, ExampleDecision>(
        u => `/assignments/${u.assignmentId}/evaluate`,
    ),
    resetAssignment: POST<{ assignmentId: Id }, AssignmentResponse>(
        u => `/assignments/${u.assignmentId}/reset`,
    ),
};
