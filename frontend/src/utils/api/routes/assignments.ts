import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AssignmentInit, type AssignmentFromServer, type AssignmentInfo } from '@/types/assignment';
import { type DecisionInit } from '@/types/decision';

const assignments = {
    create: POST<Empty, AssignmentFromServer, AssignmentInit>(
        () => `/dummy/assignments`,
    ),
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

export default assignments;
