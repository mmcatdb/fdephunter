import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { LatticeDisplay } from '@/components/dataset/FDGraphDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { DecisionProvider } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router';
import { Button, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';
import { mockAPI } from '@/utils/api/mockAPI';
import { MOCK_LATTICES } from '@/types/armstrongRelation';
// import { API } from '@/utils/api';

export function AssignmentPage() {
    const { assignment } = useLoaderData<AssignmentLoaded>();

    return (<>
        <TopbarContent>
            <div className='space-x-4'>
                <AssignmentTabs assignmentId={assignment.id} />

                {assignment.owner === 'worker' ? (
                    <Button as={Link} to={routes.worker.detail.resolve({ workerId: assignment.ownerId })}>
                        Back to Domain Expert
                    </Button>
                ) : (
                    <Button as={Link} to={routes.workflow.dashboard.root.resolve({ workflowId: assignment.ownerId })}>
                        Back to Workflow
                    </Button>
                )}
            </div>
        </TopbarContent>

        <DecisionProvider relation={assignment.relation} isFinished={assignment.isFinished}>
            <Page>
                <Outlet />
            </Page>
        </DecisionProvider>
    </>);
}

type AssignmentLoaded = {
    assignment: Assignment;
};

AssignmentPage.loader = async ({ params: { assignmentId } }: { params: Params<'assignmentId'> }): Promise<AssignmentLoaded> => {
    if (!assignmentId)
        throw new Error('Missing assignment ID');

    // const response = await API.assignments.get(undefined, { assignmentId });
    const response = await mockAPI.assignments.get(assignmentId);
    if (!response.status)
        throw new Error('Failed to load assignment');

    return {
        assignment: Assignment.fromServer(response.data),
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
