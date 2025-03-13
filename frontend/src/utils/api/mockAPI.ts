import { type DataResult, type Result } from '@/types/api/result';
import { type AssignmentInfo, AssignmentVerdict, type AssignmentFromServer, type AssignmentInit } from '@/types/assignment';
import { DecisionStatus, type DecisionInit } from '@/types/decision';
import { WorkflowState, type WorkflowFromServer } from '@/types/workflow';
import { type CreateJobResponse } from './routes/workflows';
import { JobState, type ExecuteDiscoveryParams, type ExecuteRediscoveryParams, type JobFromServer } from '@/types/job';
import { v4 } from 'uuid';
import { DateTime } from 'luxon';
import { type JobResultFromServer } from '@/types/jobResult';
import { type ExampleRelation, ExampleState, MOCK_ARMSTRONG_RELATIONS } from '@/types/armstrongRelation';

export const mockAPI = {
    assignments: {
        create: createAssignment,
        get: getAssignment,
        getAllAssignments,
        evaluate: evaluateAssignment,
        reset: resetAssignment,
    },
    workflows: {
        create: createWorkflow,
        get: getWorkflow,
        executeDiscovery,
        executeRediscovery,
        acceptAllExamples,
        getLastJob,
        getLastJobResult,
    },
};

// Assignments

type AssignmentDB = Omit<AssignmentFromServer, 'relation'> & {
    workflowId: string;
    iteration: number;
    decision?: DecisionInit;
    jobResultId?: string;
};

async function createAssignment(init: AssignmentInit): Promise<Result<AssignmentFromServer>> {
    await wait();

    const workflow = get<WorkflowDB>(init.workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobFromServer>(workflow.jobId);
    if (!job?.resultId)
        return error();

    const assignment = createAssignmentForWorkflow(init, workflow, job.resultId);

    const relation = createExampleRelation(assignment);
    if (!relation)
        return error();

    return success({ ...assignment, relation });
}

function createAssignmentForWorkflow(init: AssignmentInit, workflow: WorkflowDB, jobResultId: string): AssignmentDB {
    const assignment: AssignmentDB = {
        id: v4(),
        workflowId: init.workflowId,
        iteration: workflow.iteration,
        rowIndex: init.rowIndex,
        owner: 'workflow',
        ownerId: init.workflowId,
        verdict: AssignmentVerdict.New,
        jobResultId,
    };

    workflow.assignmentIds.push(assignment.id);

    set(assignment);
    set(workflow);

    return assignment;
}

function createExampleRelation({ workflowId, rowIndex }: { workflowId: string, rowIndex: number }): ExampleRelation | undefined {
    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow?.jobId)
        return;
    const job = get<JobFromServer>(workflow.jobId);
    if (!job?.resultId)
        return;
    const result = get<JobResultFromServer>(job.resultId);
    if (!result)
        return;

    const relation = result.relation;

    return {
        columns: relation.columns,
        referenceRow: relation.referenceRow,
        exampleRow: relation.exampleRows[rowIndex],
    };
}

async function getAssignment(assignmentId: string): Promise<Result<AssignmentFromServer>> {
    await wait();

    const assignment = get<AssignmentDB>(assignmentId);
    if (!assignment)
        return error();

    const relation = createExampleRelation(assignment);
    return relation ? success({ ...assignment, relation }) : error();
}

async function getAllAssignments(workflowId: string): Promise<Result<AssignmentInfo[]>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const assignments = workflow.assignmentIds
        .map(id => get<AssignmentDB>(id))
        .filter(a => !!a && a.iteration === workflow.iteration);

    return success(assignments as AssignmentInfo[]);
}

async function evaluateAssignment(assignmentId: string, init: DecisionInit): Promise<Result<AssignmentFromServer>> {
    await wait();

    const assignment = get<AssignmentDB>(assignmentId);
    if (!assignment)
        return error();
    const workflow = get<WorkflowDB>(assignment.workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobFromServer>(workflow.jobId);
    if (!job?.resultId)
        return error();
    const result = get<JobResultFromServer>(job.resultId);
    if (!result)
        return error();

    assignment.decision = init;
    assignment.verdict = decisionToAssignment[init.status];

    result.relation.exampleRows[assignment.rowIndex].state = decisionToExample[init.status];

    set(assignment);
    set(result);

    const relation = createExampleRelation(assignment);
    return relation ? success({ ...assignment, relation }) : error();
}

async function resetAssignment(assignmentId: string): Promise<Result<AssignmentFromServer>> {
    await wait();

    const assignment = get<AssignmentDB>(assignmentId);
    if (!assignment?.jobResultId)
        return error();
    const result = get<JobResultFromServer>(assignment.jobResultId);
    if (!result)
        return error();

    assignment.verdict = AssignmentVerdict.New;
    assignment.decision = undefined;

    result.relation.exampleRows[assignment.rowIndex].state = ExampleState.New;

    set(assignment);
    set(result);

    const relation = createExampleRelation(assignment);
    return relation ? success({ ...assignment, relation }) : error();
}

const decisionToAssignment = {
    [DecisionStatus.Accepted]: AssignmentVerdict.Accepted,
    [DecisionStatus.Rejected]: AssignmentVerdict.Rejected,
    [DecisionStatus.Unanswered]: AssignmentVerdict.DontKnow,
};

const decisionToExample = {
    [DecisionStatus.Accepted]: ExampleState.Accepted,
    [DecisionStatus.Rejected]: ExampleState.Rejected,
    [DecisionStatus.Unanswered]: ExampleState.Answered,
};

// Workflows

type WorkflowDB = WorkflowFromServer & {
    jobId?: string;
    assignmentIds: string[];
};

async function createWorkflow(): Promise<Result<WorkflowFromServer>> {
    await wait();

    const workflow: WorkflowDB = {
        id: v4(),
        state: WorkflowState.InitialSettings,
        iteration: 0,
        datasetName: undefined,
        assignmentIds: [],
    };

    set(workflow);

    return success(workflow);
}

async function getWorkflow(workflowId: string): Promise<Result<WorkflowFromServer>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);

    return workflow ? success(workflow) : error();
}

async function executeDiscovery(workflowId: string, params: ExecuteDiscoveryParams): Promise<Result<CreateJobResponse>> {
    await wait();

    // Just silence unused argument warning.
    if (!params.approach)
        return error('Missing approach');

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const job: JobFromServer = {
        id: v4(),
        state: JobState.Waiting,
        description: 'Wait for FD discovery ...',
        iteration: workflow.iteration,
        startedAt: DateTime.now().toUTC().toISO(),
    };

    workflow.state = WorkflowState.WaitingForInitialFDs;
    workflow.datasetName = params.datasetName;
    workflow.jobId = job.id;

    set(workflow);
    set(job);

    return success({
        workflow,
        job,
    });
}

async function executeRediscovery(workflowId: string, params: ExecuteRediscoveryParams, isFast?: boolean): Promise<Result<CreateJobResponse>> {
    await wait();

    // Just silence unused argument warning.
    if (!params.approach)
        return error('Missing approach');

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    workflow.iteration++;

    const job: JobFromServer = {
        id: v4(),
        state: JobState.Waiting,
        description: workflow.iteration === 1 ? 'Wait for negative example generation ...' : 'Applying approved negative examples ...',
        iteration: workflow.iteration,
        startedAt: DateTime.now().toUTC().toISO(),
    };

    workflow.jobId = job.id;
    if (isFast)
        finishJob(job, workflow);

    set(workflow);
    set(job);

    return success({
        workflow,
        job,
    });
}

async function acceptAllExamples(workflowId: string): Promise<Result<WorkflowFromServer>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobFromServer>(workflow.jobId);
    if (!job?.resultId)
        return error();
    const result = get<JobResultFromServer>(job.resultId);
    if (!result)
        return error();

    const availableIndexes: number[] = [];
    for (let i = 0; i < result.relation.exampleRows.length; i++) {
        const row = result.relation.exampleRows[i];
        const isRowAvailable = row.state === ExampleState.New && (row.isPositive === result.relation.isEvaluatingPositives);
        if (isRowAvailable)
            availableIndexes.push(i);
    }

    const assignments = workflow.assignmentIds
        .map(id => get<AssignmentDB>(id))
        .filter(a => !!a && a.iteration === workflow.iteration) as AssignmentDB[];
    const openAssignments = assignments.filter(a => a.verdict === AssignmentVerdict.New);

    for (const rowIndex of availableIndexes) {
        const assignment = openAssignments.find(a => a.rowIndex === rowIndex);
        if (!assignment) {
            const init = {
                workerId: undefined,
                workflowId,
                rowIndex,
            };

            const assignment = createAssignmentForWorkflow(init, workflow, job.resultId);
            openAssignments.push(assignment);
        }
    }

    for (const assignment of openAssignments) {
        const init = { status: DecisionStatus.Accepted, columns: [] };

        assignment.verdict = decisionToAssignment[init.status];
        assignment.decision = init;
        result.relation.exampleRows[assignment.rowIndex].state = decisionToExample[init.status];

        set(assignment);
    }

    set(result);

    return success(workflow);
}

async function getLastJob(workflowId: string): Promise<Result<JobFromServer>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow || !workflow.jobId)
        return error();

    const job = get<JobFromServer>(workflow.jobId);
    if (!job)
        return error();

    const createdAt = DateTime.fromISO(job.startedAt);
    const now = DateTime.now();

    switch (job.state) {
    case JobState.Waiting: {
        // const delay = (1 + Math.random()) * 2000;
        const delay = 0;
        if (+createdAt + delay < +now) {
            job.state = JobState.Running;
            set(job);
        }
        break;
    }
    case JobState.Running: {
        // const delay = (1 + Math.random()) * 6000;
        const delay = 0;
        if (+createdAt + delay < +now) {
            finishJob(job, workflow);

            set(job);
            set(workflow);
        }
        break;
    }
    }

    return success(job);
}

function finishJob(job: JobFromServer, workflow: WorkflowDB) {
    console.log('FINISH JOB', job, workflow);
    job.state = JobState.Finished;
    workflow.state = workflow.iteration < 4
        ? WorkflowState.NegativeExamples
        : workflow.iteration < MOCK_ARMSTRONG_RELATIONS.length
            ? WorkflowState.PositiveExamples
            : WorkflowState.DisplayFinalFDs;
}

async function getLastJobResult(workflowId: string): Promise<Result<JobResultFromServer>> {
    const jobResponse = await getLastJob(workflowId);
    if (!jobResponse.status)
        return error();

    const job = jobResponse.data;

    if (job.resultId) {
        const result = get<JobResultFromServer>(job.resultId);
        return result ? success(result) : error();
    }

    if (job.state !== JobState.Finished)
        return error('Job is not finished yet');

    const workflow = get<WorkflowDB>(workflowId)!;
    const result: JobResultFromServer = {
        id: v4(),
        payload: 'TODO',
        relation: MOCK_ARMSTRONG_RELATIONS[workflow.iteration],
    };

    job.resultId = result.id;

    set(result);
    set(job);

    return success(result);
}

// Utils

async function wait() {
    return new Promise(resolve => setTimeout(resolve, (1 + Math.random()) * 100));
}

function get<T>(key: string): T | undefined {
    const value = window.localStorage.getItem(key);
    return value === null ? undefined : JSON.parse(value) as T;
}

function set<T extends { id: string }>(value: T) {
    window.localStorage.setItem(value.id, JSON.stringify(value));
}

function success<T>(data: T): DataResult<T> {
    return { status: true, data };
}

function error<T>(error = 'Entity not found'): DataResult<T> {
    return { status: false, error };
}
