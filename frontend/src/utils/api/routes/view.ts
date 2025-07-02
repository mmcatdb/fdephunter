import { GET } from '../routeFunctions';
import { type Lattice } from '@/types/armstrongRelation';
import { type FdSet } from '@/types/functionalDependency';
import { type Id } from '@/types/id';

export const view = {
    getFds: GET<{ workflowId: Id }, FdSet>(
        u => `/workflows/${u.workflowId}/fds`,
    ),
    getLattices: GET<{ workflowId: Id }, Lattice[]>(
        u => `/workflows/${u.workflowId}/lattices`,
    ),
};
