import { useMemo, useState } from 'react';
import type { Dataset } from '@/types/dataset';
import { useApproaches, useDatasets } from '@/hooks';
import { type Approach } from '@/types/approach';
import { Workflow } from '@/types/workflow';
import { API } from '@/utils/api';
import { Job } from '@/types/job';
import { Button, Card, CardBody, Select, SelectItem, type SharedSelection } from '@nextui-org/react';
import { Page } from '@/components/layout';

type InitialSettingsProps = {
    workflow: Workflow;
    onNextStep: (workflow: Workflow, job: Job) => void;
};

export function InitialSettings({ workflow, onNextStep }: InitialSettingsProps) {
    const [ fetching, setFetching ] = useState(false);

    async function runInitialDiscovery(settings: DiscoverySettings) {
        setFetching(true);
        const response = await API.workflows.executeDiscovery({ workflowId: workflow.id }, {
            datasets: settings.datasets.map(d => d.name),
            approach: settings.approach.name,
            description: settings.description,
        });
        setFetching(false);
        if (!response.status)
            return;

        onNextStep(
            Workflow.fromServer(response.data.workflow),
            Job.fromServer(response.data.job),
        );
    }

    return (
        <Page className='flex flex-col justify-center max-w-xl'>
            <h1 className='text-lg'>Initial settings</h1>

            <Card className='mt-12 w-full'>
                <CardBody>
                    <InitialSettingsForm onSubmit={runInitialDiscovery} fetching={fetching} />
                </CardBody>
            </Card>
        </Page>
    );
}

type DiscoverySettings = {
    datasets: Dataset[];
    approach: Approach;
    description: '';
};

type InitialSettingsFormProps = {
    onSubmit: (settings: DiscoverySettings) => void;
    fetching: boolean;
};

export function InitialSettingsForm({ onSubmit, fetching }: InitialSettingsFormProps) {
    const availableDatasets = useDatasets();
    const [ selectedDatasets, setSelectedDatasets ] = useState(new Set<string>());
    const datasetOptions = useMemo(() => availableDatasets?.map(d => nameToOption(d.name)), [ availableDatasets ]);

    const availableApproaches = useApproaches();
    const [ selectedApproach, setSelectedApproach ] = useState(new Set<string>());
    const approachOptions = useMemo(() => availableApproaches?.map(a => nameToOption(a.name)), [ availableApproaches ]);

    function submit() {
        if (!availableDatasets || !availableApproaches)
            return;

        const datasets = [ ...selectedDatasets.values() ]
            .map(dataset => availableDatasets.find(d => d.name === dataset))
            .filter((d): d is Dataset => !!d);
        const selectedApproachName = selectedApproach.values().next().value;
        const approach = availableApproaches.find(a => a.name === selectedApproachName);

        if (datasets.length === 0 || !approach)
            return;

        onSubmit({
            datasets,
            approach,
            description: '', // TODO description
        });
    }

    if (!datasetOptions || !approachOptions)
        return null;

    return (<>
        <div>
            <Select
                label='Datasets'
                selectionMode='multiple'
                items={datasetOptions}
                selectedKeys={selectedDatasets}
                onSelectionChange={setSelectedDatasets as (keys: SharedSelection) => void}
            >
                {option => (
                    <SelectItem key={option.key}>{option.label}</SelectItem>
                )}
            </Select>
        </div>

        <div className='mt-4'>
            <Select
                label='Approach'
                selectionMode='single'
                items={approachOptions}
                selectedKeys={selectedApproach}
                onSelectionChange={setSelectedApproach as (keys: SharedSelection) => void}
            >
                {option => (
                    <SelectItem key={option.key}>{option.label}</SelectItem>
                )}
            </Select>
        </div>

        <Button
            className='mt-12 w-full'
            color='primary'
            onPress={submit}
            isLoading={fetching}
            isDisabled={selectedApproach.size === 0 || selectedDatasets.size === 0}
        >
            Run!
        </Button>
    </>);
}

export type Option = {
    key: string;
    label: string;
};

export function nameToOption(name: string): Option {
    return {
        key: name,
        label: name,
    };
}
