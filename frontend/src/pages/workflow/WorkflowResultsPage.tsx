import { Tab, Tabs } from '@nextui-org/react';
import { type WorkflowLoaded } from './WorkflowPage';
import { Link, matchPath, Outlet, useLocation, useRouteLoaderData } from 'react-router';
import { routes } from '@/router';
import { Page, TopbarContent } from '@/components/layout';

export function WorkflowResultsPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;

    return (<>
        <TopbarContent>
            <WorkflowResultsTabs workflowId={workflow.id} />
        </TopbarContent>

        <Page>
            <Outlet />
        </Page>
    </>);
}

function WorkflowResultsTabs({ workflowId }: { workflowId: string }) {
    const { pathname } = useLocation();
    const selectedKey = matchPath(routes.workflow.results.tabs.path, pathname)?.params.tab ?? 'index';

    return (
        <Tabs selectedKey={selectedKey}>
            <Tab key='dataset' title='Dataset' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'dataset' }) }} />
            <Tab key='list' title='Functional dependencies' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'list' }) }} />
            <Tab key='graph' title='Graph view' {...{ as: Link, to: routes.workflow.results.tabs.resolve({ workflowId, tab: 'graph' }) }} />
        </Tabs>
    );
}
