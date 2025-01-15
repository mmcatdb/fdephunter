import { DatasetTableView } from '@/components/dataset/DatasetTableView';
import { FDListView } from '@/components/dataset/FDListView';
import { FDGraphView } from '@/components/dataset/FDGraphView';
import { useClasses, useJobResult } from '@/hooks';
import { WorkersDistribution } from '@/components/WorkersDistribution';
import { type Workflow, type Class } from '@/types/workflow';
import { type Job } from '@/types/job';
import { type Approach } from '@/types/approach';
import { Tab, Tabs } from '@nextui-org/react';
import { Page } from '@/components/layout';

export type DisplayFDsProps = {
    workflow: Workflow;
    chachedClasses?: Class[];
    onNextStep: (workflow: Workflow, job: Job) => void;
}

export function DisplayFDs({ workflow, chachedClasses, onNextStep }: DisplayFDsProps) {
    const classes = useClasses(workflow.id, chachedClasses);

    const jobResult = useJobResult(workflow.id);

    // TODO Move tabs to the topbar (if possible). It would require using a separate page for each tab, but it should be doable with the new react-router api.

    return (
        <Page>
            <Tabs defaultSelectedKey='overview'>
                <Tab key='overview' title='Overview'>
                    <WorkersDistribution workflow={workflow} classes={classes} onNextStep={onNextStep} />
                </Tab>
                <Tab key='table' title='Dataset'>
                    <DatasetTableView workflowId={workflow.id} />
                </Tab>
                <Tab key='list' title='Functional dependencies'>
                    <FDListView graph={jobResult?.fdGraph} />
                </Tab>
                <Tab key='graph' title='Graph view'>
                    <FDGraphView graph={jobResult?.fdGraph} />
                </Tab>
            </Tabs>
        </Page>);
}

export type RediscoverySettings = {
    approach: Approach;
};
