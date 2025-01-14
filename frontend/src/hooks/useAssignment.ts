import { useCallback, useEffect, useState } from 'react';
import { API } from '@/utils/api';
import { Assignment, type AssignmentInfo } from '@/types/assignment';

type UseAssignmentReturn = {
    assignment: Assignment | undefined;
    setAssignment: (assignment: Assignment) => void;
};

export function useAssignment(assignmentId: string): UseAssignmentReturn {
    const [ assignment, setAssignment ] = useState<Assignment>();

    const fetchAssignments = useCallback(async (signal?: AbortSignal) => {
        const response = await API.assignments.get(signal, { assignmentId });
        if (!response.status)
            return;

        setAssignment(Assignment.fromServer(response.data));
    }, [ assignmentId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        void fetchAssignments(signal);

        return abort;
    }, [ fetchAssignments ]);

    return {
        assignment,
        setAssignment,
    };
}

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

