import { Button, Form, Modal, Tab, Tabs } from 'react-bootstrap';
import DatasetTableView from '@/components/dataset/DatasetTableView';
import FDListView from '@/components/dataset/FDListView';
import FDGraphView from '@/components/dataset/FDGraphView';
import { useApproaches, useClasses, useJobResult } from '@/hooks';
import WorkersDistribution from '@/components/WorkersDistribution';
import { Workflow, type Class } from '@/types/workflow';
import { useCallback, useMemo, useState } from 'react';
import { NegativeExampleState } from '@/types/negativeExample';
import API from '@/utils/api';
import { Job } from '@/types/job';
import { type Approach } from '@/types/approach';
import { type SingleValue } from 'react-select';
import { FormSelect } from '@/components/common/FormSelect';
import SpinnerButton from '@/components/common/SpinnerButton';
import { type Option, nameToOption } from './InitialSettings';

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
        <Tabs
            defaultActiveKey='overview'
        >
            <Tab eventKey='overview' title='Overview' className='pt-4'>
                <WorkersDistribution workflow={workflow} classes={classes} />
            </Tab>
            <Tab eventKey='table' title='Dataset' className='pt-3'>
                <DatasetTableView workflowId={workflow.id} />
            </Tab>
            <Tab eventKey='list' title='Functional dependencies' className='pt-3'>
                <FDListView graph={jobResult?.fdGraph} />
            </Tab>
            <Tab eventKey='graph' title='Graph view' className='pt-3'>
                <FDGraphView graph={jobResult?.fdGraph} />
            </Tab>
        </Tabs>
        <div className='mt-5'>
            <Button onClick={() => setShowModal(true)} disabled={!canGoNext}>
                Go next
            </Button>
        </div>
        <Modal show={showModal} onHide={() => setShowModal(false)}>
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
    const [ selectedApproach, setSelectedApproach ] = useState<Option>();
    const approachOptions = useMemo(() => availableApproaches?.map(a => nameToOption(a.name)), [ availableApproaches ]);

    function submit() {
        if (!availableApproaches)
            return;

        const approach = availableApproaches.find(a => a.name === selectedApproach?.value);
        if (!approach)
            return;

        onSubmit(approach);
    }

    const handleApproachChange = useCallback((option: SingleValue<Option>) => {
        setSelectedApproach(option ?? undefined);
    }, []);

    return (<>
        <Modal.Header>
            <Modal.Title>Execute rediscovery</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <Form.Group>
                <Form.Label>Approach</Form.Label>
                <FormSelect
                    options={approachOptions}
                    value={selectedApproach}
                    onChange={handleApproachChange}
                />
            </Form.Group>
        </Modal.Body>
        <Modal.Footer className='justify-content-start'>
            <SpinnerButton
                onClick={submit}
                fetching={fetching}
                disabled={!selectedApproach}
            >
                Execute
            </SpinnerButton>
        </Modal.Footer>
    </>);
}
