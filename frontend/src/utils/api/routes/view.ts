import { GET } from '../routeFunctions';
import { type Lattice } from '@/types/examples';
import { type FdSetResponse } from '@/types/functionalDependency';
import { type Id } from '@/types/id';

export const view = {
    getFds: GET<{ workflowId: Id }, FdSetResponse>(
        u => `/workflows/${u.workflowId}/fds`,
    ),
    getLattices: GET<{ workflowId: Id }, Lattice[]>(
        u => `/workflows/${u.workflowId}/lattices`,
    ),
};
