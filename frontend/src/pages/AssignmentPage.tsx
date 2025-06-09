import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { DecisionProvider } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router';
import { Button, Tab, Tabs } from '@heroui/react';
import { Page, Sidebar, TopbarContent } from '@/components/layout';
import { mockAPI } from '@/utils/api/mockAPI';
import { MOCK_LATTICES } from '@/types/armstrongRelation';
import { WorkflowProgressDisplay } from '@/components/worklow/WorkflowProgressDisplay';
import { Workflow } from '@/types/workflow';
// import { API } from '@/utils/api';

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

                <Button as={Link} to={routes.workflow.dashboard.root.resolve({ workflowId: assignment.workflowId })}>
                    Back to Workflow
                </Button>
            </div>
        </TopbarContent>

        <DecisionProvider relation={assignment.relation} inputDecision={assignment.decision}>
            <Page>
                <Outlet />
            </Page>
        </DecisionProvider>
    </>);
}

type AssignmentLoaded = {
    assignment: Assignment;
    /** @deprecated Workflow shouldn't be available from the assignment. */
    workflow: Workflow;
};

AssignmentPage.loader = async ({ params: { assignmentId } }: { params: Params<'assignmentId'> }): Promise<AssignmentLoaded> => {
    if (!assignmentId)
        throw new Error('Missing assignment ID');

    // const response = await API.assignments.get(undefined, { assignmentId });
    const response = await mockAPI.assignments.get(assignmentId);
    if (!response.status)
        throw new Error('Failed to load assignment');

    const workflowResponse = await mockAPI.workflows.get(response.data.workflowId);
    if (!workflowResponse.status)
        throw new Error('Failed to load workflow');

    return {
        assignment: Assignment.fromServer(response.data),
        workflow: Workflow.fromServer(workflowResponse.data),
    };
};

function AssignmentTabs({ assignmentId }: { assignmentId: string }) {
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
    //     <FDListDisplay graph={assignment.discoveryResult.fdGraph.edges} />
    // );

    return (
        <div />
    );
}

export function AssignmentGraphPage() {
    const { assignment } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    return (
        <LatticeDisplay lattices={MOCK_LATTICES[0]} />
    );
}
