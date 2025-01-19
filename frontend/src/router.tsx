import { createBrowserRouter } from 'react-router-dom';
import { LandingPage } from './pages/LandingPage';
import { WorkflowPage } from './pages/WorkflowPage';
import { WorkerPage } from './pages/WorkerPage';
import { AssignmentEvaluationPage, AssignmentGraphPage, AssignmentListPage, AssignmentPage } from './pages/AssignmentPage';
import { ExamplePage } from './pages/ExamplePage';
import { Layout } from './components/layout';

export class NamedRoute<T extends string = never> {
    constructor(
        public readonly path: string,
    ) {}

    resolve(params: Record<T, string>): string {
        let value = this.path;
        for (const key of Object.keys(params))
            value = value.replace(':' + key, params[key as keyof typeof params]);

        return value;
    }
}

export type NamedParams<TRoute> = TRoute extends NamedRoute<infer TParams> ? Record<TParams, string> : never;

export const routes = {
    root: '/',
    landing: '/',
    workflow: {
        example: '/workflows/example',
        detail: new NamedRoute<'workflowId'>('/workflows/:workflowId'),
    },
    worker: {
        example: '/workers/example',
        detail: new NamedRoute<'workerId'>('/workers/:workerId'),
    },
    assignment: {
        $id: 'assignment',
        example: '/assignments/example',
        root: new NamedRoute<'assignmentId'>('/assignments/:assignmentId'),
        tabs: new NamedRoute<'assignmentId'>('/assignments/:assignmentId/:tab'),
        evaluation: new NamedRoute<'assignmentId'>('/assignments/:assignmentId/evaluation'),
        list: new NamedRoute<'assignmentId'>('/assignments/:assignmentId/list'),
        graph: new NamedRoute<'assignmentId'>('/assignments/:assignmentId/graph'),
    },
};

export const router = createBrowserRouter([ {
    path: routes.root,
    element: <Layout />,
    children: [ {
        index: true,
        element: <LandingPage />,
    }, {
        path: routes.workflow.example,
        element: <ExamplePage type='workflow' />,
    }, {
        path: routes.workflow.detail.path,
        element: <WorkflowPage />,
    }, {
        path: routes.worker.example,
        element: <ExamplePage type='worker' />,
    }, {
        path: routes.worker.detail.path,
        element: <WorkerPage />,
    }, {
        path: routes.assignment.example,
        element: <ExamplePage type='assignment' />,
    }, {
        id: routes.assignment.$id,
        path: routes.assignment.root.path,
        element: <AssignmentPage />,
        loader: AssignmentPage.loader,
        shouldRevalidate: () => false,
        children: [ {
            path: routes.assignment.evaluation.path,
            element: <AssignmentEvaluationPage />,
        }, {
            path: routes.assignment.list.path,
            element: <AssignmentListPage />,
        }, {
            path: routes.assignment.graph.path,
            element: <AssignmentGraphPage />,
        } ],
    } ],
} ]);
