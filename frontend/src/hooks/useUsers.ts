import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { User } from '@/types/user';


export function useUsers(): User[] | undefined {
    const [ users, setUsers ] = useState<User[]>();

    const fetchUsers = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workers.getAllExpertUsers(signal, {});
        if (!response.status)
            return;

        setUsers(response.data.map(User.fromServer));
    }, []);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchUsers(signal);

        return abort;
    }, [ fetchUsers ]);

    return users;
}
