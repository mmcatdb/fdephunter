import { useCallback, useMemo, useState } from 'react';
import { API } from '@/utils/api/api';
import { Button, Card, CardBody, Select, SelectItem, type SharedSelection } from '@heroui/react';
import { Page } from '@/components/layout';
import { Link, useLoaderData, useNavigate, useRouteLoaderData } from 'react-router';
import { routes } from '@/router';
import { type WorkflowLoaded } from './WorkflowPage';
import { WorkflowState } from '@/types/workflow';
import { FileInput, type FileInputValue } from '@/components/common/FileInput';
import { type DatasetResponse } from '@/types/dataset';

export function WorkflowSettingsPage() {
    const { workflow } = useRouteLoaderData<WorkflowLoaded>(routes.workflow.$id)!;
    const { datasets } = useLoaderData<WorkflowSettingsLoaded>();

    const [ fetching, setFetching ] = useState(false);
    const navigate = useNavigate();

    async function runInitialDiscovery(settings: DiscoverySettings) {
        setFetching(true);
        const response = await API.workflow.startWorkflow({ workflowId: workflow.id }, {
            description: `Initial discovery for ${settings.dataset.name}`,
            datasetId: settings.dataset.id,
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
    datasets: DatasetResponse[];
};

WorkflowSettingsPage.loader = async (): Promise<WorkflowSettingsLoaded> => {
    const datasetsResponse = await API.dataset.getDatasets(undefined, {});
    if (!datasetsResponse.status)
        throw new Error('Failed to load datasets');

    return {
        datasets: datasetsResponse.data,
    };
};

type DiscoverySettings = {
    dataset: DatasetResponse;
};

type InitialSettingsFormProps = {
    datasets: DatasetResponse[];
    onSubmit: (settings: DiscoverySettings) => void;
    fetching: boolean;
};

function InitialSettingsForm({ datasets, onSubmit, fetching }: InitialSettingsFormProps) {
    const [ selected, setSelected ] = useState({
        dataset: new Set<string>(),
        file: undefined as FileInputValue,
    });
    const datasetOptions = useMemo(() => datasets.map(datasetToOption), []);

    function submit() {
        if (!selected.dataset.size && !selected.file)
            return;
        // const finalDatasets = [ ...selectedDatasets.values() ]
        //     .map(dataset => datasets.find(d => d.name === dataset))
        //     .filter((d): d is Dataset => !!d);

        // if (finalDatasets.length === 0)
        //     return;

        // FIXME Use file as dataset.
        const datasetId = selected.dataset.values().next().value!;
        const dataset = datasets.find(d => d.id === datasetId)!;

        onSubmit({
            // FIXME Use file as dataset.
            // datasetName: selected.file?.originalName ?? selected.dataset.values().next().value!,
            dataset,
        });
    }

    const setSelectedValue = useCallback((value: FileInputValue | SharedSelection) => {
        if (value === 'all')
            return;

        if (value instanceof Set)
            setSelected(() => ({ dataset: value as Set<string>, file: undefined }));
        else
            setSelected(() => ({ dataset: new Set(), file: value }));

    }, []);

    return (<>
        <p className='mb-4 text-center'>Pick from the predefined options ...</p>

        <div>
            <Select
                label='Dataset'
                items={datasetOptions}
                selectedKeys={selected.dataset}
                onSelectionChange={setSelectedValue}
            >
                {option => (
                    <SelectItem key={option.key}>{option.label}</SelectItem>
                )}
            </Select>
        </div>

        <p className='my-4 text-center'>... or upload your own!</p>

        <FileInput value={selected.file} onChange={setSelectedValue} />

        <Button
            className='mt-8 w-full'
            color='primary'
            onPress={submit}
            isLoading={fetching}
            isDisabled={!selected.dataset.size && !selected.file}
        >
            Run!
        </Button>
    </>);
}

type Option = {
    key: string;
    label: string;
};

function datasetToOption(dataset: DatasetResponse): Option {
    return {
        key: dataset.id,
        label: dataset.name,
    };
}
