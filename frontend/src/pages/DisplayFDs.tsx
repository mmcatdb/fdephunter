import { useMemo, useState } from 'react';
import { DatasetTableView } from '@/components/dataset/DatasetTableView';
import { FDListView } from '@/components/dataset/FDListView';
import { FDGraphView } from '@/components/dataset/FDGraphView';
import { useApproaches, useClasses, useJobResult } from '@/hooks';
import { WorkersDistribution } from '@/components/WorkersDistribution';
import { Workflow, type Class } from '@/types/workflow';
import { NegativeExampleState } from '@/types/negativeExample';
import { API } from '@/utils/api';
import { Job } from '@/types/job';
import { type Approach } from '@/types/approach';
import { nameToOption } from './InitialSettings';
import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Select, SelectItem, type SharedSelection, Tab, Tabs } from '@nextui-org/react';

export type DisplayFDsProps = {
    workflow: Workflow;
    chachedClasses?: Class[];
    onNextStep: (workflow: Workflow, job: Job) => void;
}

export default function DisplayFDs({ workflow, chachedClasses, onNextStep }: DisplayFDsProps) {
    const classes = useClasses(workflow.id, chachedClasses);

    const jobResult = useJobResult(workflow.id);
    const canGoNext = useMemo(() => {
        const acceptedClasses = classes.filter(c => c.example && c.example.state === NegativeExampleState.Accepted);
        return classes.length === acceptedClasses.length;
    }, [ classes ]);

    const [ showModal, setShowModal ] = useState(false);
    const [ fetching, setFetching ] = useState(false);

    async function runRediscovery(approach: Approach) {
        setFetching(true);
        const response = await API.workflows.executeRediscovery({ workflowId: workflow.id }, {
            approach: approach.name,
        });
        setFetching(false);
        if (!response.status)
            return;

        onNextStep(
            Workflow.fromServer(response.data.workflow),
            Job.fromServer(response.data.job),
        );
    }

    return (<>
        <Tabs defaultSelectedKey='overview'>
            <Tab key='overview' title='Overview' className='pt-6'>
                <WorkersDistribution workflow={workflow} classes={classes} />
            </Tab>
            <Tab key='table' title='Dataset' className='pt-4'>
                <DatasetTableView workflowId={workflow.id} />
            </Tab>
            <Tab key='list' title='Functional dependencies' className='pt-4'>
                <FDListView graph={jobResult?.fdGraph} />
            </Tab>
            <Tab key='graph' title='Graph view' className='pt-4'>
                <FDGraphView graph={jobResult?.fdGraph} />
            </Tab>
        </Tabs>
        <div className='mt-12'>
            <Button onPress={() => setShowModal(true)} disabled={!canGoNext}>
                Go next
            </Button>
        </div>
        <Modal isOpen={showModal} onClose={() => setShowModal(false)}>
            <RediscoveryFormModal onSubmit={runRediscovery} fetching={fetching} />
        </Modal>
    </>);
}

export type RediscoverySettings = {
    approach: Approach;
};

type RediscoveryFormModalProps = {
    onSubmit: (approach: Approach) => void;
    fetching?: boolean;
};

export function RediscoveryFormModal({ onSubmit, fetching }: RediscoveryFormModalProps) {
    const availableApproaches = useApproaches();
    const [ selectedApproach, setSelectedApproach ] = useState(new Set<string>());
    const approachOptions = useMemo(() => availableApproaches?.map(a => nameToOption(a.name)), [ availableApproaches ]);

    function submit() {
        if (!availableApproaches)
            return;

        const selectedApproachName = selectedApproach.values().next().value;
        const approach = availableApproaches.find(a => a.name === selectedApproachName);
        if (!approach)
            return;

        onSubmit(approach);
    }

    if (!approachOptions)
        return null;

    return (
        <ModalContent>
            <ModalHeader>
                Execute rediscovery
            </ModalHeader>

            <ModalBody>
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
            </ModalBody>

            <ModalFooter className='justify-start'>
                <Button
                    onPress={submit}
                    isLoading={fetching}
                    disabled={!selectedApproach}
                >
                    Execute
                </Button>
            </ModalFooter>
        </ModalContent>
    );
}
