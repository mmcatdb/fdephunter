import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { LatticeDisplay } from '@/components/dataset/FdGraphDisplay';
import { DecisionProvider } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router';
import { Button, Tab, Tabs } from '@heroui/react';
import { Page, Sidebar, TopbarContent } from '@/components/layout';
import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
import { type Lattice } from '@/types/examples';
import { type Id } from '@/types/id';
import { API } from '@/utils/api/api';

export function AssignmentPage() {
    const { assignment, workflow } = useLoaderData<AssignmentLoaded>();

    return (<>
        {/* FIXME This shouldn't be here ... */}
        <Sidebar>
            <WorkflowProgressDisplay currentStep={workflow.state} />
        </Sidebar>

        <TopbarContent>
            <div className='space-x-4'>
                <AssignmentTabs assignmentId={assignment.id} />

                {/* FIXME */}
                <Button as={Link} to={routes.workflow.dashboard.root.resolve({ workflowId: assignment.workflowId })}>
                    Back to Workflow
                </Button>

                <Link to={routes.workflow.dashboard.root.resolve({ workflowId: assignment.workflowId })}>
                    <Button>
                        Back to Workflow
                    </Button>
                </Link>
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
};

AssignmentPage.loader = async ({ params: { assignmentId } }: { params: Params<'assignmentId'> }): Promise<AssignmentLoaded> => {
    if (!assignmentId)
        throw new Error('Missing assignment ID');

    const response = await API.assignment.getAssignment(undefined, { assignmentId });
    if (!response.status)
        throw new Error('Failed to load assignment');

    const workflowResponse = await API.workflow.getWorkflow(undefined, { workflowId: response.data.workflowId });
    if (!workflowResponse.status)
        throw new Error('Failed to load workflow');

    const latticesResponse = await API.view.getLattices(undefined, { workflowId: response.data.workflowId });
    if (!latticesResponse.status)
        throw new Error('Failed to load lattices');

    return {
        assignment: Assignment.fromResponse(response.data),
        workflow: Workflow.fromResponse(workflowResponse.data),
        lattices: latticesResponse.data,
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
    // FIXME This needs to work.

    // const { assignment } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    // return (
    //     <FdListDisplay graph={assignment.jobResult.fdGraph.edges} />
    // );

    return (
        <div />
    );
}

export function AssignmentGraphPage() {
    const { lattices } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    return (
        <LatticeDisplay lattices={lattices} />
    );
}
