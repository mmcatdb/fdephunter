import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { type AssignmentInfo } from '@/types/assignment';

export function useAnsweredAssignments(workerId: string): AssignmentInfo[] | undefined {
    const [ answeredAssignments, setAnsweredAssignments ] = useState<AssignmentInfo[]>();

    const fetchAssignments = useCallback(async (signal?: AbortSignal) => {
        const response = await API.assignments.getAllAnswered(signal, { workerId });
        if (!response.status)
            return;

        setAnsweredAssignments(response.data);
    }, [ workerId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchAssignments(signal);

        return abort;
    }, [ fetchAssignments ]);

    return answeredAssignments;
}

