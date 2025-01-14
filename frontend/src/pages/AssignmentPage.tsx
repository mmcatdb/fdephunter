import DatasetTable from '@/components/DatasetTable';
import AssignmentEvaluation from '@/components/AssignmentEvaluation';
import FDGraphView from '@/components/dataset/FDGraphView';
import FDListView from '@/components/dataset/FDListView';
import { DecisionProvider } from '@/context/DecisionProvider';
import { useAssignment } from '@/hooks';
import { routes, type NamedParams } from '@/router';
import { type Assignment } from '@/types/assignment';
import { createDataWithExamples, type DatasetDataWithExamples } from '@/types/dataset';
import { useMemo } from 'react';
import { Button, Tab, Tabs } from 'react-bootstrap';
import { useParams } from 'react-router';
import Portal, { portals } from '@/components/common/Portal';
import { Link } from 'react-router-dom';

export default function AssignmentPage() {
    const { assignmentId } = useParams() as NamedParams<typeof routes.assignment.detail>;
    const { assignment, setAssignment } = useAssignment(assignmentId);
    const data = useMemo(() => createDataWithExamples(assignment), [ assignment ]);

    if (!assignment || !data)
        return null;

    return (<>
        <Portal to={portals.topbar}>
            <Link to={routes.worker.detail.resolve({ workerId: assignment.workerId })}>
                <Button>Back to domain expert</Button>
            </Link>
        </Portal>
        <DecisionProvider data={data} isFinished={assignment.isFinished}>
            <AssignmentReady assignment={assignment} setAssignment={setAssignment} data={data} />
        </DecisionProvider>
    </>);
}

type AssignmentReadyProps = {
    assignment: Assignment;
    setAssignment: (assignment: Assignment) => void;
    data: DatasetDataWithExamples;
}

function AssignmentReady({ assignment, setAssignment, data }: AssignmentReadyProps) {
    return (<>
        <Tabs
            defaultActiveKey='evaluation'
        >
            <Tab eventKey='evaluation' title='Evaluation' className='pt-4'>
                <DatasetTable
                    data={data}
                />
                <AssignmentEvaluation
                    assignment={assignment}
                    onEvaluated={setAssignment}
                />
            </Tab>
            <Tab eventKey='list' title='Functional dependencies' className='pt-3'>
                <FDListView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
            <Tab eventKey='graph' title='Graph view' className='pt-3'>
                <FDGraphView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
        </Tabs>
    </>);
}