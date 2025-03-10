import type { Empty } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type ApproachFromServer } from '@/types/approach';
import { type FileFromServer } from '@/types/file';

export const approaches = {
    getAll: GET<Empty, ApproachFromServer[]>(
        () => `/approaches`,
    ),
    // FIXME Not implemented yet on the backend!
    uploadFile: POST<Empty, FileFromServer, FormData>(
        () => `/files`,
    ),
};
