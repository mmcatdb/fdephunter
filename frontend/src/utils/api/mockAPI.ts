import { type DataResult, type Result } from '@/types/api/result';
import { type AssignmentInfo, AssignmentState, type AssignmentResponse, type AssignmentInit, DecisionStatus, type ExampleDecision, DecisionColumnStatus } from '@/types/assignment';
import { WorkflowState, type WorkflowResponse } from '@/types/workflow';
import { type CreateJobResponse } from './routes/workflows';
import { type JobResultResponse, JobState, type ExecuteDiscoveryParams, type ExecuteRediscoveryParams, type JobResponse } from '@/types/job';
import { v4 } from 'uuid';
import { DateTime } from 'luxon';
import { type ArmstrongRelation, type ExampleRelation, ExampleState, type Lattice } from '@/types/armstrongRelation';
import { MOCK_ARMSTRONG_RELATIONS, MOCK_DATASET_DATA, MOCK_DATASETS, MOCK_FDS, MOCK_LATTICES, type MockFDClass } from '../mockData';
import { type DatasetData, type DatasetResponse } from '@/types/dataset';

export const mockAPI = {
    assignments: {
        create: createAssignment,
        get: getAssignment,
        getAllAssignments,
        evaluate: evaluateAssignment,
        reset: resetAssignment,
        getLattices,
    },
    workflows: {
        create: createWorkflow,
        get: getWorkflow,
        executeDiscovery,
        executeRediscovery,
        acceptAllExamples,
        getLastJob,
        getLastJobResult,
        getDatasetData,
        getFdClasses,
    },
    datasets: {
        getAll: getAllDatasets,
    },
};

// Assignments

type AssignmentDB = Omit<AssignmentResponse, 'relation'> & {
    workflowId: string;
    iteration: number;
    jobResultId?: string;
};

async function createAssignment(init: AssignmentInit): Promise<Result<AssignmentResponse>> {
    await wait();

    const workflow = get<WorkflowDB>(init.workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobResponse>(workflow.jobId);
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
        state: AssignmentState.New,
        jobResultId,
        decision: undefined,
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
    const job = get<JobResponse>(workflow.jobId);
    if (!job?.resultId)
        return;
    const result = get<JobResultResponse>(job.resultId);
    if (!result)
        return;

    return armstrongRelationToExampleRelation(result.relation, rowIndex);
}

function armstrongRelationToExampleRelation(relation: ArmstrongRelation, rowIndex: number): ExampleRelation {
    return {
        columns: relation.columns,
        referenceRow: relation.referenceRow,
        exampleRow: relation.exampleRows[rowIndex],
    };
}

async function getAssignment(assignmentId: string): Promise<Result<AssignmentResponse>> {
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

async function evaluateAssignment(assignmentId: string, decision: ExampleDecision): Promise<Result<AssignmentResponse>> {
    await wait();

    const assignment = get<AssignmentDB>(assignmentId);
    if (!assignment)
        return error();
    const workflow = get<WorkflowDB>(assignment.workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobResponse>(workflow.jobId);
    if (!job?.resultId)
        return error();
    const result = get<JobResultResponse>(job.resultId);
    if (!result)
        return error();

    assignment.decision = decision;
    assignment.state = decisionToAssignment[decision.status];

    result.relation.exampleRows[assignment.rowIndex].state = decisionToExample[decision.status];

    set(assignment);
    set(result);

    return success({ ...assignment, relation: armstrongRelationToExampleRelation(result.relation, assignment.rowIndex) });
}

async function resetAssignment(assignmentId: string): Promise<Result<AssignmentResponse>> {
    await wait();

    const assignment = get<AssignmentDB>(assignmentId);
    if (!assignment?.jobResultId)
        return error();
    const result = get<JobResultResponse>(assignment.jobResultId);
    if (!result)
        return error();

    assignment.state = AssignmentState.New;
    assignment.decision = undefined;

    result.relation.exampleRows[assignment.rowIndex].state = ExampleState.New;

    set(assignment);
    set(result);

    const relation = createExampleRelation(assignment);
    return relation ? success({ ...assignment, relation }) : error();
}

const decisionToAssignment = {
    [DecisionStatus.Accepted]: AssignmentState.Accepted,
    [DecisionStatus.Rejected]: AssignmentState.Rejected,
    [DecisionStatus.Unanswered]: AssignmentState.DontKnow,
};

const decisionToExample = {
    [DecisionStatus.Accepted]: ExampleState.Accepted,
    [DecisionStatus.Rejected]: ExampleState.Rejected,
    [DecisionStatus.Unanswered]: ExampleState.Undecided,
};

// Workflows

type WorkflowDB = WorkflowResponse & {
    jobId?: string;
    assignmentIds: string[];
};

async function createWorkflow(): Promise<Result<WorkflowResponse>> {
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

async function getWorkflow(workflowId: string): Promise<Result<WorkflowResponse>> {
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

    const job: JobResponse = {
        id: v4(),
        state: JobState.Waiting,
        description: 'Wait for FD discovery ...',
        iteration: workflow.iteration,
        startedAt: DateTime.now().toUTC().toISO(),
    };

    workflow.state = WorkflowState.InitialFdDiscovery;
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

    const job: JobResponse = {
        id: v4(),
        state: JobState.Waiting,
        description: workflow.iteration === 1 ? 'Wait for negative example generation ...' : 'Applying approved examples ...',
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

async function acceptAllExamples(workflowId: string): Promise<Result<WorkflowResponse>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow?.jobId)
        return error();
    const job = get<JobResponse>(workflow.jobId);
    if (!job?.resultId)
        return error();
    const result = get<JobResultResponse>(job.resultId);
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
    const openAssignments = assignments.filter(a => a.state === AssignmentState.New);

    for (const rowIndex of availableIndexes) {
        const assignment = openAssignments.find(a => a.rowIndex === rowIndex);
        if (!assignment) {
            const init = {
                workflowId,
                rowIndex,
            };

            const assignment = createAssignmentForWorkflow(init, workflow, job.resultId);
            openAssignments.push(assignment);
        }
    }

    for (const assignment of openAssignments) {
        const relation = armstrongRelationToExampleRelation(result.relation, assignment.rowIndex);
        const columns = relation.columns.map((name, colIndex) => ({
            colIndex,
            name,
            status: relation.exampleRow.maxSetElement.includes(colIndex) ? undefined : DecisionColumnStatus.Valid,
            reasons: [],
        }));
        const init = { status: DecisionStatus.Accepted, columns } satisfies ExampleDecision;

        assignment.state = decisionToAssignment[init.status];
        assignment.decision = init;
        result.relation.exampleRows[assignment.rowIndex].state = decisionToExample[init.status];

        set(assignment);
    }

    set(result);

    return success(workflow);
}

async function getLastJob(workflowId: string): Promise<Result<JobResponse>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow || !workflow.jobId)
        return error();

    const job = get<JobResponse>(workflow.jobId);
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

function finishJob(job: JobResponse, workflow: WorkflowDB) {
    job.state = JobState.Finished;
    workflow.state = workflow.iteration < MOCK_ARMSTRONG_RELATIONS.length
        ? MOCK_ARMSTRONG_RELATIONS[workflow.iteration].isEvaluatingPositives
            ? WorkflowState.PositiveExamples
            : WorkflowState.NegativeExamples
        : WorkflowState.DisplayFinalFDs;
}

async function getLastJobResult(workflowId: string): Promise<Result<JobResultResponse>> {
    const jobResponse = await getLastJob(workflowId);
    if (!jobResponse.status)
        return error();

    const job = jobResponse.data;

    if (job.resultId) {
        const result = get<JobResultResponse>(job.resultId);
        return result ? success(result) : error();
    }

    if (job.state !== JobState.Finished)
        return error('Job is not finished yet');

    const workflow = get<WorkflowDB>(workflowId)!;
    const result: JobResultResponse = {
        id: v4(),
        payload: 'TODO',
        relation: MOCK_ARMSTRONG_RELATIONS[workflow.iteration],
    };

    job.resultId = result.id;

    set(result);
    set(job);

    return success(result);
}

async function getAllDatasets(): Promise<Result<DatasetResponse[]>> {
    await wait();

    return success(MOCK_DATASETS);
}

async function getLattices(workflowId: string): Promise<Result<Lattice[]>> {
    await wait();
    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const index = workflow.iteration === 0 ? 0 : 1;

    return success(MOCK_LATTICES[index]);
}

async function getDatasetData(workflowId: string): Promise<Result<DatasetData>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    return success(MOCK_DATASET_DATA);
}

async function getFdClasses(workflowId: string): Promise<Result<MockFDClass[]>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const index = workflow.iteration === 0 ? 0 : 1;

    return success(MOCK_FDS[index]);
}

// Utils

async function wait() {
    // return new Promise(resolve => setTimeout(resolve, (1 + Math.random()) * 10));
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
