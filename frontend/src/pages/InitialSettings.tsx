import { useCallback, useMemo, useState } from 'react';
import { Col, Container, Form, Row } from 'react-bootstrap';
import type { Dataset } from '@/types/dataset';
import { useApproaches, useDatasets } from '@/hooks';
import { type Approach } from '@/types/approach';
import { FormSelect } from '@/components/common/FormSelect';
import { type MultiValue, type SingleValue } from 'react-select';
import SpinnerButton from '@/components/common/SpinnerButton';
import { Workflow } from '@/types/workflow';
import API from '@/utils/api';
import { Job } from '@/types/job';

type InitialSettingsProps = {
    workflow: Workflow;
    onNextStep: (workflow: Workflow, job: Job) => void;
}

export default function InitialSettings({ workflow, onNextStep }: InitialSettingsProps) {
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
        <Container>
            <Row>
                <Col />
                <Col lg={4} md={6} xs={12}>
                    <h1>Initial settings</h1>
                    <InitialSettingsForm onSubmit={runInitialDiscovery} fetching={fetching} />
                </Col>
                <Col />
            </Row>
        </Container>
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
    const [ selectedDatasets, setSelectedDatasets ] = useState<readonly Option[]>([]);
    const datasetOptions = useMemo(() => availableDatasets?.map(d => nameToOption(d.name)), [ availableDatasets ]);
       
    const availableApproaches = useApproaches();
    const [ selectedApproach, setSelectedApproach ] = useState<Option>();
    const approachOptions = useMemo(() => availableApproaches?.map(a => nameToOption(a.name)), [ availableApproaches ]);

    function submit() {
        if (!availableDatasets || !availableApproaches)
            return;

        const datasets = selectedDatasets
            .map(dataset => availableDatasets.find(d => d.name === dataset.value))
            .filter((d): d is Dataset => !!d);
        const approach = availableApproaches.find(a => a.name === selectedApproach?.value);

        if (datasets.length === 0 || !approach)
            return;

        onSubmit({
            datasets,
            approach,
            description: '', // TODO description
        });
    }

    const handleDatasetChange = useCallback((options: MultiValue<Option>) => {
        setSelectedDatasets(options);
    }, []);

    const handleApproachChange = useCallback((option: SingleValue<Option>) => {
        setSelectedApproach(option ?? undefined);
    }, []);

    return (<>
        <Form.Group className='mt-3'>
            <Form.Label>Datasets</Form.Label>
            <FormSelect
                options={datasetOptions}
                value={selectedDatasets}
                onChange={handleDatasetChange}
                isMulti
            />
        </Form.Group>
        <Form.Group className='mt-3'>
            <Form.Label>Approach</Form.Label>
            <FormSelect
                options={approachOptions}
                value={selectedApproach}
                onChange={handleApproachChange}
            />
        </Form.Group>
        <SpinnerButton
            className='mt-5 w-100'
            onClick={submit}
            fetching={fetching}
            disabled={!selectedApproach || selectedDatasets.length === 0}
        >
            Run!
        </SpinnerButton>
    </>);
}

export type Option = {
    value: string;
    label: string;
};

export function nameToOption(name: string): Option {
    return {
        value: name,
        label: name,
    };
}
