import type { Empty } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import { type ApproachFromServer } from '@/types/approach';

export const approaches = {
    getAll: GET<Empty, ApproachFromServer[]>(
        () => `/approaches`,
    ),
};
