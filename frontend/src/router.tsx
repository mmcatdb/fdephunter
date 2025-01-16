import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { LandingPage } from './pages/LandingPage';
import { WorkflowPage } from './pages/WorkflowPage';
import { WorkerPage } from './pages/WorkerPage';
import { AssignmentPage } from './pages/AssignmentPage';
import { ExamplePage } from './pages/ExamplePage';

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
        example: '/assignments/example',
        detail: new NamedRoute<'assignmentId'>('/assignments/:assignmentId'),
    },
};

export function RouterView() {
    const location = useLocation();
    const from = (location.state as { from: string | undefined })?.from ?? routes.root;

    return (
        <Routes>
            <Route path={routes.landing} element={<LandingPage />} />

            <Route path={routes.workflow.example}       element={<ExamplePage type='workflow' />} />
            <Route path={routes.workflow.detail.path}   element={<WorkflowPage />} />

            <Route path={routes.worker.example}         element={<ExamplePage type='worker' />} />
            <Route path={routes.worker.detail.path}     element={<WorkerPage />} />

            <Route path={routes.assignment.example}     element={<ExamplePage type='assignment' />} />
            <Route path={routes.assignment.detail.path} element={<AssignmentPage />} />

            <Route path='*' element={<Navigate replace to={from} />} />
        </Routes>
    );
}
