import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { FDGraphDisplay } from '@/components/dataset/FDGraphDisplay';
import { FDListDisplay } from '@/components/dataset/FDListDisplay';
import { DecisionProvider, useDecisionContext } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { createDataWithExamples } from '@/types/dataset';
import { useMemo } from 'react';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router';
import { Button, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';
import { DatasetTable } from '@/components/dataset/DatasetTableDisplay';
import { API } from '@/utils/api';

export function AssignmentPage() {
    const { assignment } = useLoaderData<AssignmentLoaded>();
    const data = useMemo(() => createDataWithExamples(assignment), [ assignment ]);

    return (<>
        <TopbarContent>
            <div className='space-x-4'>
                <AssignmentTabs assignmentId={assignment.id} />

                <Button as={Link} to={routes.worker.detail.resolve({ workerId: assignment.workerId })}>
                    Back to Domain Expert
                </Button>
            </div>
        </TopbarContent>

        <DecisionProvider data={data} isFinished={assignment.isFinished}>
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

    const response = await API.assignments.get(undefined, { assignmentId });
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
    const { decision: { data } } = useDecisionContext();
    const revalidator = useRevalidator();

    return (
        <div className='space-y-4'>
            <DatasetTable data={data} />

            <AssignmentEvaluation assignment={assignment} onEvaluated={() => revalidator.revalidate()} />
        </div>
    );
}

export function AssignmentListPage() {
    const { assignment } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    return (
        <FDListDisplay graph={assignment.discoveryResult.fdGraph} />
    );
}

export function AssignmentGraphPage() {
    const { assignment } = useRouteLoaderData<AssignmentLoaded>(routes.assignment.$id)!;

    return (
        <FDGraphDisplay graph={assignment.discoveryResult.fdGraph} />
    );
}
