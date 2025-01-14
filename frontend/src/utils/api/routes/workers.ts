import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type WorkerFromServer } from '@/types/worker';
import { type UserFromServer } from '@/types/user';

const workers = {
    get: GET<{ workerId: StringLike }, WorkerFromServer>(
        u => `/domain-experts/${u.workerId}`,
    ),
    accept: POST<{ workerId: StringLike }, WorkerFromServer>(
        u => `/domain-experts/${u.workerId}/accept`,
    ),
    reject: POST<{ workerId: StringLike }, WorkerFromServer>(
        u => `/domain-experts/${u.workerId}/reject`,
    ),
    getAllExpertUsers: GET<Empty, UserFromServer[]>(
        () => `/users/experts`,
    ),
};

export default workers;
