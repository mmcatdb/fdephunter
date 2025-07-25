import { createBrowserRouter } from 'react-router';
import { LandingPage } from './pages/LandingPage';
import { WorkflowPage } from './pages/workflow/WorkflowPage';
import { AssignmentEvaluationPage, AssignmentGraphPage, AssignmentListPage, AssignmentPage } from './pages/AssignmentPage';
import { Layout } from './components/layout';
import { WorkflowSettingsPage } from './pages/workflow/WorkflowSettingsPage';
import { WorkflowJobPage } from './pages/workflow/WorkflowJobPage';
import { WorkflowOverviewPage, WorkflowDashboardPage, WorkflowDatasetPage, WorkflowGraphPage, WorkflowListPage } from './pages/workflow/WorkflowDashboardPage';
import { WorkflowFinalPage, WorkflowResultsPage } from './pages/workflow/WorkflowResultsPage';

export class NamedRoute<T extends string = never> {
    constructor(
        public readonly path: string,
    ) {}

    resolve(params: Record<T, string>): string {
        return this.resolvePartial(params as Partial<Record<T, string>>);
    }

    resolvePartial(params: Partial<Record<T, string>>): string {
        let value = this.path;
        for (const key of Object.keys(params))
            value = value.replace(':' + key, params[key as keyof typeof params]!);

        return value;
    }
}

export type NamedParams<TRoute> = TRoute extends NamedRoute<infer TParams> ? Record<TParams, string> : never;

export const routes = {
    root: '/',
    landing: '/',
    workflow: {
        $id: 'workflow',
        root: new NamedRoute<'workflowId'>('/workflows/:workflowId'),
        settings: new NamedRoute<'workflowId'>('/workflows/:workflowId/settings'),
        job: new NamedRoute<'workflowId'>('/workflows/:workflowId/job'),
        dashboard: {
            $id: 'dashboard',
            root: new NamedRoute<'workflowId'>('/workflows/:workflowId/dashboard'),
            tabs: new NamedRoute<'workflowId' | 'tab'>('/workflows/:workflowId/dashboard/:tab'),
        },
        results: {
            $id: 'results',
            root: new NamedRoute<'workflowId'>('/workflows/:workflowId/results'),
            tabs: new NamedRoute<'workflowId' | 'tab'>('/workflows/:workflowId/results/:tab'),
        },
    },
    assignment: {
        $id: 'assignment',
        example: '/assignments/example',
        root: new NamedRoute<'assignmentId'>('/assignments/:assignmentId'),
        tabs: new NamedRoute<'assignmentId' | 'tab'>('/assignments/:assignmentId/:tab'),
    },
};

export const router = createBrowserRouter([ {
    path: routes.root,
    element: <Layout />,
    children: [ {
        index: true,
        element: <LandingPage />,
    }, {
        path: routes.workflow.root.path,
        element: <WorkflowPage />,
        loader: WorkflowPage.loader,
        id: routes.workflow.$id,
        shouldRevalidate: ({ defaultShouldRevalidate }) => defaultShouldRevalidate,
        children: [ {
            path: routes.workflow.settings.path,
            element: <WorkflowSettingsPage />,
            loader: WorkflowSettingsPage.loader,
        }, {
            path: routes.workflow.job.path,
            element: <WorkflowJobPage />,
            loader: WorkflowJobPage.loader,
        }, {
            path: routes.workflow.dashboard.root.path,
            element: <WorkflowDashboardPage />,
            loader: WorkflowDashboardPage.loader,
            id: routes.workflow.dashboard.$id,
            shouldRevalidate: ({ defaultShouldRevalidate }) => defaultShouldRevalidate,
            children: [ {
                index: true,
                element: <WorkflowOverviewPage />,
            }, {
                path: routes.workflow.dashboard.tabs.resolvePartial({ tab: 'dataset' }),
                element: <WorkflowDatasetPage />,
                loader: WorkflowDatasetPage.loader,
            }, {
                path: routes.workflow.dashboard.tabs.resolvePartial({ tab: 'list' }),
                element: <WorkflowListPage />,
                loader: WorkflowListPage.loader,
            }, {
                path: routes.workflow.dashboard.tabs.resolvePartial({ tab: 'graph' }),
                element: <WorkflowGraphPage />,
                loader: WorkflowGraphPage.loader,
            } ],
        }, {
            path: routes.workflow.results.root.path,
            element: <WorkflowResultsPage />,
            id: routes.workflow.results.$id,
            children: [ {
                index: true,
                element: <WorkflowFinalPage />,
                loader: WorkflowFinalPage.loader,
            }, {
                path: routes.workflow.results.tabs.resolvePartial({ tab: 'dataset' }),
                element: <WorkflowDatasetPage />,
                loader: WorkflowDatasetPage.loader,
            }, {
                path: routes.workflow.results.tabs.resolvePartial({ tab: 'list' }),
                element: <WorkflowListPage />,
                loader: WorkflowListPage.loader,
            }, {
                path: routes.workflow.results.tabs.resolvePartial({ tab: 'graph' }),
                element: <WorkflowGraphPage />,
                loader: WorkflowGraphPage.loader,
            } ],
        } ],
    }, {
        path: routes.assignment.root.path,
        element: <AssignmentPage />,
        loader: AssignmentPage.loader,
        id: routes.assignment.$id,
        shouldRevalidate: () => false,
        children: [ {
            index: true,
            element: <AssignmentEvaluationPage />,
        }, {
            path: routes.assignment.tabs.resolvePartial({ tab: 'list' }),
            element: <AssignmentListPage />,
        }, {
            path: routes.assignment.tabs.resolvePartial({ tab: 'graph' }),
            element: <AssignmentGraphPage />,
        } ],
    } ],
} ]);
