import type { StringLike } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import { type Lattice } from '@/types/armstrongRelation';
import { type FdSet } from '@/types/functionalDependency';

export const view = {
    getFds: GET<{ workflowId: StringLike }, FdSet>(
        u => `/workflows/${u.workflowId}/fds`,
    ),
    getLattices: GET<{ workflowId: StringLike }, Lattice[]>(
        u => `/workflows/${u.workflowId}/lattices`,
    ),
};
