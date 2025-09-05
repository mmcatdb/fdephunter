import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { LatticesDisplay } from '@/components/dataset/LatticesDisplay';
import { DecisionProvider } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router';
import { Button, Card, CardBody, Tab, Tabs } from '@heroui/react';
import { Page, Sidebar, TopbarContent } from '@/components/layout';
import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
import { Lattice } from '@/types/examples';
import { type Id } from '@/types/id';
import { API } from '@/utils/api/api';
import { FdSet } from '@/types/functionalDependency';
import { FdListDisplay } from '@/components/dataset/FdListDisplay';
import { useMemo } from 'react';
import { createFdEdges } from './workflow/WorkflowResultsPage';

export function AssignmentPage() {
    const { assignment, workflow } = useLoaderData<AssignmentLoaded>();

    return (<>
        <Sidebar>
            <WorkflowProgressDisplay currentStep={workflow.state} />
        </Sidebar>

        <TopbarContent>
            <div className='space-x-4'>
                <AssignmentTabs assignmentId={assignment.id} />

                <Button as={Link} to={routes.workflow.dashboard.root.resolve({ workflowId: assignment.workflowId })}>
                    Back to Workflow
                </Button>
            </div>
        </TopbarContent>

        <DecisionProvider assignment={assignment} inputDecision={assignment.exampleRow.decision}>
            <Page>
                <Outlet />
            </Page>
        </DecisionProvider>
    </>);
}

type AssignmentLoaded = {
    assignment: Assignment;
    workflow: Workflow;
    lattices: Lattice[];
    fdSet: FdSet;
};

AssignmentPage.loader = async ({ params: { assignmentId } }: { params: Params<'assignmentId'> }): Promise<AssignmentLoaded> => {
    if (!assignmentId)
        throw new Error('Missing assignment ID');

    const response = await API.assignment.getAssignment(undefined, { assignmentId });
    if (!response.status)
        throw new Error('Failed to load assignment');

    const workflowId =  response.data.workflowId;

    const [ workflowResponse, latticesResponse, fdSetResponse ] = await Promise.all([
        API.workflow.getWorkflow(undefined, { workflowId }),
        API.view.getLattices(undefined, { workflowId }),
        API.view.getFds(undefined, { workflowId }),
    ]);

    if (!workflowResponse.status)
        throw new Error('Failed to load workflow');
    if (!latticesResponse.status)
        throw new Error('Failed to load lattices');
    if (!fdSetResponse.status)
        throw new Error('Failed to load functional dependencies');

    return {
        assignment: Assignment.fromResponse(response.data),
        workflow: Workflow.fromResponse(workflowResponse.data),
        lattices: latticesResponse.data.lattices.map(Lattice.fromResponse),
        fdSet: FdSet.fromResponse(fdSetResponse.data),
    };
};

function AssignmentTabs({ assignmentId }: { assignmentId: Id }) {
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.assignment.tabs.path, pathname)?.params.tab ?? 'index';

    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='index' title='Evaluation' {...{ as: Link, to: routes.assignment.root.resolve({ assignmentId }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.assignment.tabs.resolve({ assignmentId, tab: 'list' }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.assignment.tabs.resolve({ assignmentId, tab: 'graph' }) } } />
        </Tabs>
    );
}

export function AssignmentEvaluationPage() {
    const { assignment } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;
    const revalidator = useRevalidator();

    return (
        <AssignmentEvaluation assignment={assignment} onEvaluated={() => revalidator.revalidate()} />
    );
}

export function AssignmentListPage() {
    const { fdSet } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    const fds = useMemo(() => createFdEdges(fdSet), [ fdSet ]);

    return (
        <Card className='mx-auto w-fit'>
            <CardBody>
                <FdListDisplay edges={fds} />
            </CardBody>
        </Card>
    );
}

export function AssignmentGraphPage() {
    const { lattices } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    return (
        <LatticesDisplay lattices={lattices} />
    );
}
