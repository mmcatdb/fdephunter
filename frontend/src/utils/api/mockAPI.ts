import { type DataResult, type Result } from '@/types/api/result';
import { WorkflowState, type WorkflowResponse } from '@/types/workflow';
import { type StartWorkflowRequest, type CreateJobResponse, type ContinueWorkflowRequest } from './routes/workflow';
import { JobState, type JobResponse } from '@/types/job';
import { v4 } from 'uuid';
import { DateTime } from 'luxon';
import { type Lattice } from '@/types/armstrongRelation';
import { MOCK_ARMSTRONG_RELATIONS, MOCK_DATASET_DATA, MOCK_FD_SETS, MOCK_LATTICES } from '../mockData';
import { type DatasetData } from '@/types/dataset';
import { type FdSet } from '@/types/functionalDependency';
import { type Id } from '@/types/id';

export const mockAPI = {
    dataset: {
        // getDatasets,
        getDatasetData,
    },
    assignment: {
        // getAssignment,
        // getAssignments,
        // evaluateAssignment,
        // resetAssignment,
    },
    workflow: {
        startWorkflow,
        continueWorkflow,
        // acceptAllAssignments,
        getLastJob,
    },
    view: {
        getFds,
        getLattices,
    },
};

// Assignments

// type AssignmentDB = Omit<AssignmentResponse, 'relation'> & {
//     workflowId: Id;
//     iteration: number;
//     jobResultId?: string;
// };

// async function createAssignment(init: AssignmentInit): Promise<Result<AssignmentResponse>> {
//     await wait();

//     const workflow = get<WorkflowDB>(init.workflowId);
//     if (!workflow?.jobId)
//         return error();
//     const job = get<JobResponse>(workflow.jobId);
//     if (!job?.resultId)
//         return error();

//     const assignment = createAssignmentForWorkflow(init, workflow, job.resultId);

//     const relation = createExampleRelation(assignment);
//     if (!relation)
//         return error();

//     return success({ ...assignment, relation });
// }

// type AssignmentInit = {
//     workflowId: Id;
//     rowIndex: number;
// };

// function createAssignmentForWorkflow(init: AssignmentInit, workflow: WorkflowDB, jobResultId: Id): AssignmentDB {
//     const assignment: AssignmentDB = {
//         id: v4(),
//         workflowId: init.workflowId,
//         iteration: workflow.iteration,
//         rowIndex: init.rowIndex,
//         jobResultId,
//         decision: undefined,
//     };

//     workflow.assignmentIds.push(assignment.id);

//     set(assignment);
//     set(workflow);

//     return assignment;
// }

// function createExampleRelation({ workflowId, rowIndex }: { workflowId: Id, rowIndex: number }): ExampleRelation | undefined {
//     const workflow = get<WorkflowDB>(workflowId);
//     if (!workflow?.jobId)
//         return;
//     const job = get<JobResponse>(workflow.jobId);
//     if (!job?.resultId)
//         return;
//     const result = get<JobResultResponse>(job.resultId);
//     if (!result)
//         return;

//     return armstrongRelationToExampleRelation(result.relation, rowIndex);
// }

// function armstrongRelationToExampleRelation(relation: ArmstrongRelation, rowIndex: number): ExampleRelation {
//     return {
//         columns: relation.columns,
//         referenceRow: relation.referenceRow,
//         exampleRow: relation.exampleRows[rowIndex],
//     };
// }

// async function getAssignment(assignmentId: Id): Promise<Result<AssignmentResponse>> {
//     await wait();

//     const assignment = get<AssignmentDB>(assignmentId);
//     if (!assignment)
//         return error();

//     const relation = createExampleRelation(assignment);
//     return relation ? success({ ...assignment, relation }) : error();
// }

// async function getAssignments(workflowId: Id): Promise<Result<AssignmentResponse[]>> {
//     await wait();

//     const workflow = get<WorkflowDB>(workflowId);
//     if (!workflow)
//         return error();

//     const assignments = workflow.assignmentIds
//         .map(id => get<AssignmentDB>(id))
//         .filter(a => !!a && a.iteration === workflow.iteration);

//     return success(assignments as AssignmentResponse[]);
// }

// async function evaluateAssignment(assignmentId: Id, decision: ExampleDecision): Promise<Result<AssignmentResponse>> {
//     await wait();

//     const assignment = get<AssignmentDB>(assignmentId);
//     if (!assignment)
//         return error();
//     const workflow = get<WorkflowDB>(assignment.workflowId);
//     if (!workflow?.jobId)
//         return error();
//     const job = get<JobResponse>(workflow.jobId);
//     if (!job?.resultId)
//         return error();
//     const result = get<JobResultResponse>(job.resultId);
//     if (!result)
//         return error();

//     assignment.decision = decision;
//     assignment.state = decisionToAssignment[decision.status];

//     result.relation.exampleRows[assignment.rowIndex].decision = decision;

//     set(assignment);
//     set(result);

//     return success({ ...assignment, relation: armstrongRelationToExampleRelation(result.relation, assignment.rowIndex) });
// }

// async function resetAssignment(assignmentId: Id): Promise<Result<AssignmentResponse>> {
//     await wait();

//     const assignment = get<AssignmentDB>(assignmentId);
//     if (!assignment?.jobResultId)
//         return error();
//     const result = get<JobResultResponse>(assignment.jobResultId);
//     if (!result)
//         return error();

//     result.relation.exampleRows[assignment.rowIndex].decision = undefined;

//     set(assignment);
//     set(result);

//     const relation = createExampleRelation(assignment);
//     return relation ? success({ ...assignment, relation }) : error();
// }

// Workflows

type WorkflowDB = WorkflowResponse & {
    jobId?: string;
    assignmentIds: string[];
};

// async function createWorkflow(): Promise<Result<WorkflowResponse>> {
//     await wait();

//     const workflow: WorkflowDB = {
//         id: v4(),
//         state: WorkflowState.InitialSettings,
//         iteration: 0,
//         datasetId: undefined,
//         assignmentIds: [],
//     };

//     set(workflow);

//     return success(workflow);
// }

// async function getWorkflow(workflowId: Id): Promise<Result<WorkflowResponse>> {
//     await wait();

//     const workflow = get<WorkflowDB>(workflowId);

//     return workflow ? success(workflow) : error();
// }

async function startWorkflow(workflowId: Id, request: StartWorkflowRequest): Promise<Result<CreateJobResponse>> {
    await wait();

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
    workflow.datasetId = request.datasetId;
    workflow.jobId = job.id;

    set(workflow);
    set(job);

    return success({
        workflow,
        job,
    });
}

async function continueWorkflow(workflowId: Id, request: ContinueWorkflowRequest): Promise<Result<CreateJobResponse>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    workflow.iteration++;

    const job: JobResponse = {
        id: v4(),
        state: JobState.Waiting,
        description: request.description,
        iteration: workflow.iteration,
        startedAt: DateTime.now().toUTC().toISO(),
    };

    workflow.jobId = job.id;

    set(workflow);
    set(job);

    return success({
        workflow,
        job,
    });
}

// async function acceptAllAssignments(workflowId: Id): Promise<Result<WorkflowResponse>> {
//     await wait();

//     const workflow = get<WorkflowDB>(workflowId);
//     if (!workflow?.jobId)
//         return error();
//     const job = get<JobResponse>(workflow.jobId);
//     if (!job?.resultId)
//         return error();
//     const result = get<JobResultResponse>(job.resultId);
//     if (!result)
//         return error();

//     const availableIndexes: number[] = [];
//     for (let i = 0; i < result.relation.exampleRows.length; i++) {
//         const row = result.relation.exampleRows[i];
//         const isRowAvailable = !row.decision && (row.isPositive === result.relation.isEvaluatingPositives);
//         if (isRowAvailable)
//             availableIndexes.push(i);
//     }

//     const assignments = workflow.assignmentIds
//         .map(id => get<AssignmentDB>(id))
//         .filter(a => !!a && a.iteration === workflow.iteration) as AssignmentDB[];
//     const openAssignments = assignments.filter(a => !a.relation.exampleRow.decision);

//     for (const rowIndex of availableIndexes) {
//         const assignment = openAssignments.find(a => a.rowIndex === rowIndex);
//         if (!assignment) {
//             const init = {
//                 workflowId,
//                 rowIndex,
//             };

//             const assignment = createAssignmentForWorkflow(init, workflow, job.resultId);
//             openAssignments.push(assignment);
//         }
//     }

//     for (const assignment of openAssignments) {
//         const relation = armstrongRelationToExampleRelation(result.relation, assignment.rowIndex);
//         const columns = relation.columns.map((_, colIndex) => ({
//             status: relation.exampleRow.maxSetElement.columns.includes(colIndex) ? undefined : DecisionColumnStatus.Valid,
//             reasons: [],
//         }));
//         const init = { status: DecisionStatus.Accepted, columns } satisfies ExampleDecision;

//         assignment.decision = init;
//         result.relation.exampleRows[assignment.rowIndex].decision = init;

//         set(assignment);
//     }

//     set(result);

//     return success(workflow);
// }

async function getLastJob(workflowId: Id): Promise<Result<JobResponse>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow || !workflow.jobId)
        return error();

    const job = get<JobResponse>(workflow.jobId);
    if (!job)
        return error();

    const createdAt = DateTime.fromISO(job.startedAt!);
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
    job.finishedAt = DateTime.now().toUTC().toISO();
    workflow.state = workflow.iteration < MOCK_ARMSTRONG_RELATIONS.length
        ? MOCK_ARMSTRONG_RELATIONS[workflow.iteration].isEvaluatingPositives
            ? WorkflowState.PositiveExamples
            : WorkflowState.NegativeExamples
        : WorkflowState.DisplayFinalFds;
}

// async function getLastJobResult(workflowId: Id): Promise<Result<JobResultResponse>> {
//     const jobResponse = await getLastJob(workflowId);
//     if (!jobResponse.status)
//         return error();

//     const job = jobResponse.data;

//     if (job.resultId) {
//         const result = get<JobResultResponse>(job.resultId);
//         return result ? success(result) : error();
//     }

//     if (job.state !== JobState.Finished)
//         return error('Job is not finished yet');

//     const workflow = get<WorkflowDB>(workflowId)!;
//     const result: JobResultResponse = {
//         id: v4(),
//         relation: MOCK_ARMSTRONG_RELATIONS[workflow.iteration],
//     };

//     job.resultId = result.id;

//     set(result);
//     set(job);

//     return success(result);
// }

// async function getDatasets(): Promise<Result<DatasetResponse[]>> {
//     await wait();

//     return success(MOCK_DATASETS);
// }

async function getLattices(workflowId: Id): Promise<Result<Lattice[]>> {
    await wait();
    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const index = workflow.iteration === 0 ? 0 : 1;

    return success(MOCK_LATTICES[index]);
}

async function getDatasetData(workflowId: Id): Promise<Result<DatasetData>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    return success(MOCK_DATASET_DATA);
}

async function getFds(workflowId: Id): Promise<Result<FdSet>> {
    await wait();

    const workflow = get<WorkflowDB>(workflowId);
    if (!workflow)
        return error();

    const index = workflow.iteration === 0 ? 0 : 1;

    return success(MOCK_FD_SETS[index]);
}

// Utils

async function wait() {
    // return new Promise(resolve => setTimeout(resolve, (1 + Math.random()) * 10));
}

function get<T>(key: string): T | undefined {
    const value = window.localStorage.getItem(key);
    return value === null ? undefined : JSON.parse(value) as T;
}

function set<T extends { id: Id }>(value: T) {
    window.localStorage.setItem(value.id, JSON.stringify(value));
}

function success<T>(data: T): DataResult<T> {
    return { status: true, data };
}

function error<T>(error = 'Entity not found'): DataResult<T> {
    return { status: false, error };
}
