import { useCallback, useEffect, useState } from 'react';
import API from '@/utils/api';
import { Class, Workflow } from '@/types/workflow';
import { Job } from '@/types/job';

type WorkflowCache = {
    job?: Job;
    classes?: Class[];
}

type UseWorkflowReturn = {
    workflow?: Workflow;
    cache: WorkflowCache;
    setWorkflow: (workflow: Workflow, cache?: WorkflowCache) => void;
    reload: () => void;
};

export function useWorkflow(workflowId: string): UseWorkflowReturn {
    const [ workflow, setWorkflow ] = useState<Workflow>();
    const [ cache, setCache ] = useState<WorkflowCache>({});

    const setWorkflowWithCache = useCallback((workflow: Workflow, cache?: WorkflowCache) => {
        setWorkflow(workflow);
        if (cache)
            setCache(cache);
    }, []);

    const fetchWorkflows = useCallback(async (signal?: AbortSignal) => {
        const response = await API.workflows.get(signal, { workflowId });
        if (!response.status) 
            return;

        setWorkflow(Workflow.fromServer(response.data));
        setCache({
            job: response.data.job && Job.fromServer(response.data.job),
            classes: response.data.classes && response.data.classes.map(Class.fromServer),
        });
    }, [ workflowId ]);

    useEffect(() => {
        const [ signal, abort ] = API.prepareAbort();
        fetchWorkflows(signal);

        return abort;
    }, [ fetchWorkflows ]);

    const reload = useCallback(() => fetchWorkflows(), [ fetchWorkflows ]);

    return {
        workflow,
        cache,
        setWorkflow: setWorkflowWithCache,
        reload,
    };
}