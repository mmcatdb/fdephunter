import { DatasetTable } from '@/components/DatasetTable';
import { AssignmentEvaluation } from '@/components/AssignmentEvaluation';
import { FDGraphView } from '@/components/dataset/FDGraphView';
import { FDListView } from '@/components/dataset/FDListView';
import { DecisionProvider } from '@/context/DecisionProvider';
import { useAssignment } from '@/hooks';
import { routes, type NamedParams } from '@/router';
import { type Assignment } from '@/types/assignment';
import { createDataWithExamples, type DatasetDataWithExamples } from '@/types/dataset';
import { useMemo } from 'react';
import { Portal } from '@/components/common/Portal';
import { Link, useParams } from 'react-router-dom';
import { Button, Tab, Tabs } from '@nextui-org/react';

export function AssignmentPage() {
    const { assignmentId } = useParams() as NamedParams<typeof routes.assignment.detail>;
    const { assignment, setAssignment } = useAssignment(assignmentId);
    const data = useMemo(() => createDataWithExamples(assignment), [ assignment ]);

    if (!assignment || !data)
        return null;

    return (<>
        <Portal to={Portal.targets.topbar}>
            <Button as={Link} to={routes.worker.detail.resolve({ workerId: assignment.workerId })}>
                Back to domain expert
            </Button>
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
};

function AssignmentReady({ assignment, setAssignment, data }: AssignmentReadyProps) {
    return (
        <Tabs defaultSelectedKey='evaluation'>
            <Tab key='evaluation' title='Evaluation' className='pt-6'>
                <DatasetTable
                    data={data}
                />
                <AssignmentEvaluation
                    assignment={assignment}
                    onEvaluated={setAssignment}
                />
            </Tab>
            <Tab key='list' title='Functional dependencies' className='pt-4'>
                <FDListView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
            <Tab key='graph' title='Graph view' className='pt-4'>
                <FDGraphView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
        </Tabs>
    );
}
