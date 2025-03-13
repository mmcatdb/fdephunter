import { useState } from 'react';
import { Dataset, DatasetType } from '@/types/dataset';
import { Approach } from '@/types/approach';
// import { API } from '@/utils/api';
import { Button, Card, CardBody } from '@nextui-org/react';
import { Page } from '@/components/layout';
import { Link, useLoaderData, useNavigate, useRouteLoaderData } from 'react-router';
import { routes } from '@/router';
import { type WorkflowLoaded } from './WorkflowPage';
import { WorkflowState } from '@/types/workflow';
import { FileInput } from '@/components/common/FileInput';
import { type FileFromServer } from '@/types/file';
import { mockAPI } from '@/utils/api/mockAPI';

export function WorkflowSettingsPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const { datasets, approaches } = useLoaderData<WorkflowSettingsLoaded>();

    const [ fetching, setFetching ] = useState(false);
    const navigate = useNavigate();

    async function runInitialDiscovery(settings: DiscoverySettings) {
        setFetching(true);
        // const response = await API.workflows.executeDiscovery({ workflowId: workflow.id }, {
        const response = await mockAPI.workflows.executeDiscovery(workflow.id, {
            datasets: settings.datasets.map(d => d.name),
            approach: settings.approach.name,
            datasetName: settings.datasetName,
        });
        if (!response.status) {
            setFetching(false);
            return;
        }

        void navigate(routes.workflow.job.resolve({ workflowId: workflow.id }));
    }

    return (
        <Page className='max-w-xl'>
            <h1 className='text-lg'>Select a dataset</h1>

            {workflow.state === WorkflowState.InitialSettings ? (
                <Card className='mt-10 w-full'>
                    <CardBody>
                        <InitialSettingsForm
                            datasets={datasets}
                            approaches={approaches}
                            onSubmit={runInitialDiscovery}
                            fetching={fetching}
                        />
                    </CardBody>
                </Card>
            ) : (<>
                <div className='mt-6 flex items-center gap-6'>
                    <div>
                        Everything set up!
                    </div>

                    <Button color='primary' {...{ as: Link, to: routes.workflow.job.resolve({ workflowId: workflow.id }) }}>
                        Continue
                    </Button>
                </div>
            </>)}
        </Page>
    );
}

type WorkflowSettingsLoaded = {
    datasets: Dataset[];
    approaches: Approach[];
};

WorkflowSettingsPage.loader = async (): Promise<WorkflowSettingsLoaded> => {
    // const [ datasetsResponse, approachesResponse ] = await Promise.all([
    //     API.datasets.getAll(undefined, {}),
    //     API.approaches.getAll(undefined, {}),
    // ]);
    // if (!datasetsResponse.status)
    //     throw new Error('Failed to load datasets');
    // if (!approachesResponse.status)
    //     throw new Error('Failed to load approaches');

    // return {
    //     datasets: datasetsResponse.data.map(Dataset.fromServer),
    //     approaches: approachesResponse.data.map(Approach.fromServer),
    // };

    await new Promise(resolve => setTimeout(resolve, (1 + Math.random()) * 200));

    return {
        datasets: [ Dataset.fromServer({ name: 'temp', type: DatasetType.Csv }) ],
        approaches: [ Approach.fromServer({ name: 'temp', author: 'temp' }) ],
    };
};

type DiscoverySettings = {
    datasets: Dataset[];
    approach: Approach;
    datasetName: string;
};

type InitialSettingsFormProps = {
    datasets: Dataset[];
    approaches: Approach[];
    onSubmit: (settings: DiscoverySettings) => void;
    fetching: boolean;
};

function InitialSettingsForm({ datasets, approaches, onSubmit, fetching }: InitialSettingsFormProps) {
    const [ file, setFile ] = useState<FileFromServer>();
    // const [ selectedDatasets, setSelectedDatasets ] = useState(new Set<string>());
    // const datasetOptions = useMemo(() => datasets.map(d => nameToOption(d.name)), [ datasets ]);

    // const [ selectedApproach, setSelectedApproach ] = useState(new Set<string>());
    // const approachOptions = useMemo(() => approaches.map(a => nameToOption(a.name)), [ approaches ]);

    function submit() {
        if (!file)
            return;
        // const finalDatasets = [ ...selectedDatasets.values() ]
        //     .map(dataset => datasets.find(d => d.name === dataset))
        //     .filter((d): d is Dataset => !!d);
        // const selectedApproachName = selectedApproach.values().next().value;
        // const approach = approaches.find(a => a.name === selectedApproachName);
        const approach = approaches[0];

        // if (finalDatasets.length === 0 || !approach)
        //     return;

        onSubmit({
            // datasets: finalDatasets,
            datasets: [ datasets[0] ],
            approach,
            datasetName: file.originalName,
        });
    }

    return (<>
        {/* <div>
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
        </div> */}

        <FileInput value={file} onChange={setFile} />

        {/* <div className='mt-4'>
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
        </div> */}

        <Button
            className='mt-10 w-full'
            color='primary'
            onPress={submit}
            isLoading={fetching}
            // isDisabled={selectedApproach.size === 0 || selectedDatasets.size === 0}
            isDisabled={!file}
        >
            Run!
        </Button>
    </>);
}

type Option = {
    key: string;
    label: string;
};

function nameToOption(name: string): Option {
    return {
        key: name,
        label: name,
    };
}
