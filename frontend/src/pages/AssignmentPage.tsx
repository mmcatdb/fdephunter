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
import { Link, useParams } from 'react-router-dom';
import { Button, Tab, Tabs } from '@nextui-org/react';
import { Page, TopbarContent } from '@/components/layout';

export function AssignmentPage() {
    const { assignmentId } = useParams() as NamedParams<typeof routes.assignment.detail>;
    const { assignment, setAssignment } = useAssignment(assignmentId);
    const data = useMemo(() => createDataWithExamples(assignment), [ assignment ]);

    if (!assignment || !data)
        return null;

    return (<>
        <TopbarContent>
            <Button as={Link} to={routes.worker.detail.resolve({ workerId: assignment.workerId })}>
                Back to domain expert
            </Button>
        </TopbarContent>

        <DecisionProvider data={data} isFinished={assignment.isFinished}>
            <Page>
                <AssignmentReady assignment={assignment} setAssignment={setAssignment} data={data} />
            </Page>
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
            <Tab key='evaluation' title='Evaluation'>
                <div className='space-y-4'>
                    <DatasetTable
                        data={data}
                    />

                    <AssignmentEvaluation
                        assignment={assignment}
                        onEvaluated={setAssignment}
                    />
                </div>
            </Tab>
            <Tab key='list' title='Functional dependencies'>
                <FDListView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
            <Tab key='graph' title='Graph view'>
                <FDGraphView graph={assignment.discoveryResult.fdGraph} />
            </Tab>
        </Tabs>
    );
}
