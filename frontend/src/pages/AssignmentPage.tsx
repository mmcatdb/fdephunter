import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { FDGraphView } from '@/components/dataset/FDGraphView';
import { FDListView } from '@/components/dataset/FDListView';
import { DecisionProvider, useDecisionContext } from '@/context/DecisionProvider';
import { routes } from '@/router';
import { Assignment } from '@/types/assignment';
import { createDataWithExamples } from '@/types/dataset';
import { useMemo } from 'react';
import { Link, matchPath, Outlet, type Params, useLoaderData, useLocation, useRevalidator, useRouteLoaderData } from 'react-router-dom';
import { Button, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';
import { DatasetTable } from '@/components/dataset/DatasetTableView';
import { API } from '@/utils/api';

export function AssignmentPage() {
    const assignment = useLoaderData() as Assignment;
    const data = useMemo(() => createDataWithExamples(assignment), [ assignment ]);

    return (<>
        <TopbarContent>
            <div className='space-x-4'>
                <AssignmentTabs assignmentId={assignment.id} />

                <Button as={Link} to={routes.worker.detail.resolve({ workerId: assignment.workerId })}>
                Back to domain expert
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

AssignmentPage.loader = async ({ params: { assignmentId } }: { params: Params<'assignmentId'> }): Promise<Assignment> => {
    if (!assignmentId)
        throw new Error('Missing assignment ID');

    const response = await API.assignments.get(undefined, { assignmentId });
    if (!response.status)
        throw new Error('Failed to load assignment');

    return Assignment.fromServer(response.data);
};

function AssignmentTabs({ assignmentId }: { assignmentId: string }) {
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.assignment.tabs.path, pathname)?.params.tab;

    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='evaluation' title='Evaluation' {...{ as: Link, to: routes.assignment.evaluation.resolve({ assignmentId }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.assignment.list.resolve({ assignmentId }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.assignment.graph.resolve({ assignmentId }) } } />
        </Tabs>
    );
}

export function AssignmentEvaluationPage() {
    const assignment = useRouteLoaderData(routes.assignment.$id) as Assignment;
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
    const assignment = useRouteLoaderData(routes.assignment.$id) as Assignment;

    return (
        <FDListView graph={assignment.discoveryResult.fdGraph} />
    );
}

export function AssignmentGraphPage() {
    const assignment = useRouteLoaderData(routes.assignment.$id) as Assignment;

    return (
        <FDGraphView graph={assignment.discoveryResult.fdGraph} />
    );
}
