import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AssignmentFromServer, type AssignmentInfo } from '@/types/assignment';
import { type DecisionInit } from '@/types/decision';

export const assignments = {
    get: GET<{ assignmentId: StringLike }, AssignmentFromServer, { limit?: number }>(
        u => `/assignments/${u.assignmentId}`,
    ),
    getAllAnswered: GET<{ workerId: StringLike }, AssignmentInfo[]>(
        u => `/domain-experts/${u.workerId}/answered-assignments`,
    ),
    evaluate: POST<{ assignmentId: StringLike }, AssignmentFromServer, DecisionInit>(
        u => `/assignments/${u.assignmentId}/evaluate`,
    ),
};
