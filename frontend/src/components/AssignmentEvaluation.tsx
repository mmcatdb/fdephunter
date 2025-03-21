import { type ReactNode, useCallback, useMemo, useState } from 'react';
import { ColumnState, type DecisionColumn, DecisionPhase, useDecisionContext } from '@/context/DecisionProvider';
import { DecisionStatus } from '@/types/decision';
import { useNavigate } from 'react-router';
import { IoAdd, IoClose } from 'react-icons/io5';
import { routes } from '@/router';
import { AssignmentVerdictLabel } from './AssignmentVerdictLabel';
import { AssignmentVerdict, type Assignment } from '@/types/assignment';
// import { API } from '@/utils/api';
import { TbPointFilled } from 'react-icons/tb';
import { Button, Card, CardBody, CardFooter, CardHeader, Checkbox, Input, Switch } from '@heroui/react';
import { ExampleRelationDisplay } from './dataset/ArmstrongRelationDisplay';
import { type ExampleRelation } from '@/types/armstrongRelation';
import { ColumnNameBadge } from './dataset/FDListDisplay';
import { FaArrowRight } from 'react-icons/fa';
import { mockAPI } from '@/utils/api/mockAPI';

type AssignmentEvaluationProps = {
    assignment: Assignment;
    onEvaluated: (assignment: Assignment) => void;
};

export function AssignmentEvaluation({ assignment, onEvaluated }: AssignmentEvaluationProps) {
    const { decision } = useDecisionContext();
    const [ selectedFDIndex, setSelectedFDIndex ] = useState<number>();
    const areColsClickable = decision.phase !== DecisionPhase.AnswerYesNo || assignment.verdict === AssignmentVerdict.Accepted || assignment.verdict === AssignmentVerdict.DontKnow;

    return (
        <div className='flex flex-col items-start gap-4'>
            <ControlCard assignment={assignment} onEvaluated={onEvaluated} setSelectedFDIndex={setSelectedFDIndex} />

            {decision.phase !== DecisionPhase.JustFinished && (
                <Card className='p-4 max-w-full'>
                    <ExampleRelationDisplay
                        relation={assignment.relation}
                        selectedColIndex={selectedFDIndex}
                        setSelectedColIndex={areColsClickable ? setSelectedFDIndex : undefined}
                    />
                </Card>
            )}

            {areColsClickable && selectedFDIndex !== undefined && (
                <DecisionCard relation={assignment.relation} selectedFDIndex={selectedFDIndex} />
            )}
        </div>
    );
}

type ControlCardProps = {
    assignment: Assignment;
    onEvaluated: (assignment: Assignment) => void;
    setSelectedFDIndex: (index: number | undefined) => void;
};

function ControlCard({ assignment, onEvaluated, setSelectedFDIndex }: ControlCardProps) {
    const { decision, setDecision } = useDecisionContext();
    const [ fetching, setFetching ] = useState<string>();
    const navigate = useNavigate();

    async function evaluate(status: DecisionStatus, fid: string) {
        const columns = status === DecisionStatus.Accepted
            ? decision.columns.map(col => ({
                name: col.name,
                state: col.state === ColumnState.Undecided ? ColumnState.Valid : col.state,
                reasons: [],
            }))
            : decision.columns.map(col => {
                const trimmedReasons = col.reasons.map(reason => reason.trim()).filter(reason => reason.length > 0);
                const uniqueReasons = [ ...new Set(trimmedReasons) ];

                return {
                    name: col.name,
                    state: col.state,
                    reasons: uniqueReasons,
                };
            });

        setFetching(fid);
        // const response = await API.assignments.evaluate({ assignmentId: assignment.id }, {
        const response = await mockAPI.assignments.evaluate(assignment.id, {
            status,
            columns,
        });
        setFetching(undefined);
        if (!response.status)
            return;

        setDecision({ ...decision, phase: DecisionPhase.JustFinished });
        onEvaluated(assignment);
    }

    async function reevaluate(fid: string) {
        setFetching(fid);
        const response = await mockAPI.assignments.reset(assignment.id);
        if (!response.status) {
            setFetching(undefined);
            return;
        }

        await navigate(0);
    }

    function continueAccepted() {
        const route = assignment.owner === 'worker'
            ? routes.worker.detail.resolve({ workerId: assignment.ownerId })
            : routes.workflow.dashboard.root.resolve({ workflowId: assignment.ownerId });
        void navigate(route);
    }

    function decideRest(isValid: boolean) {
        const newState = isValid ? ColumnState.Valid : ColumnState.Invalid;

        setDecision(prev => ({
            ...prev,
            columns: prev.columns.map(col => ({ ...col, state: col.state === ColumnState.Undecided ? newState : col.state })),
        }));

        setSelectedFDIndex(undefined);
    }

    const isUndecided = decision.columns.some(column => column.state === ColumnState.Undecided);

    return (
        <Card className='self-stretch'>
            <CardHeader>
                <h1 className='text-lg font-semibold'>
                    {titles[decision.phase]}
                </h1>
            </CardHeader>

            <CardBody className='space-y-3'>
                {bodies[decision.phase]}

                {decision.phase === DecisionPhase.Finished && (
                    <p>
                        This example was evaluated as <AssignmentVerdictLabel verdict={assignment.verdict} />. Do you want to re-evaluate it?
                    </p>
                )}
            </CardBody>

            <CardFooter className='gap-3'>
                {decision.phase === DecisionPhase.AnswerYesNo && (<>
                    <Button color='success' onPress={() => evaluate(DecisionStatus.Accepted, FID_ACCEPT)} isLoading={fetching === FID_ACCEPT} isDisabled={!!fetching}>
                        Yes, the example is possible
                    </Button>
                    <Button color='danger' onPress={() => setDecision({ ...decision, phase: DecisionPhase.ProvideReason })}>
                        No, the example is not possible
                    </Button>
                    <Button color='warning' onPress={() => evaluate(DecisionStatus.Unanswered, FID_ANSWER)} isLoading={fetching === FID_ANSWER} isDisabled={!!fetching}>
                        I don't know ...
                    </Button>
                </>)}

                {decision.phase === DecisionPhase.ProvideReason && (<>
                    <Button color='primary' onPress={() => evaluate(DecisionStatus.Rejected, FID_REJECT)} isLoading={fetching === FID_REJECT} isDisabled={!!fetching || isUndecided}>
                        Submit
                    </Button>
                    <Button color='success' onPress={() => decideRest(true)} isDisabled={!isUndecided}>
                        Accept rest
                    </Button>
                    <Button color='danger' onPress={() => decideRest(false)} isDisabled={!isUndecided}>
                        Reject rest
                    </Button>
                    <Button color='warning' onPress={() => evaluate(DecisionStatus.Unanswered, FID_ANSWER)} isLoading={fetching === FID_ANSWER} isDisabled={!!fetching || !isUndecided}>
                        I don't know ...
                    </Button>
                </>)}

                {decision.phase === DecisionPhase.JustFinished && (<>
                    <Button color='primary' onPress={continueAccepted}>
                        Go back and continue
                    </Button>
                </>)}

                {decision.phase === DecisionPhase.Finished && (
                    <Button color='warning' onPress={() => reevaluate(FID_REEVALUATE)} isLoading={fetching === FID_REEVALUATE} isDisabled={!!fetching}>
                        Re-evaluate
                    </Button>
                )}
            </CardFooter>
        </Card>
    );
}

const FID_ACCEPT = 'accept';
const FID_REJECT = 'reject';
const FID_ANSWER = 'answer';
const FID_REEVALUATE = 'reevaluate';

const titles: { [key in DecisionPhase]: string } = {
    [DecisionPhase.AnswerYesNo]: 'Is this example correct?',
    [DecisionPhase.ProvideReason]: 'Why is this example incorrect?',
    [DecisionPhase.JustFinished]: 'Thank you!',
    [DecisionPhase.Finished]: 'Assignment finished',
};

const bodies: { [key in DecisionPhase]: ReactNode } = {
    [DecisionPhase.AnswerYesNo]: (<>
        <p>
            The first row is from the original dataset. The second one was generated. Please decide if the second row is a possible part of the dataset.
        </p>
        <p>
            The row should be marked as possible only if all its values are valid as well as all their combinations (with regards to all other values in the first row). If any of these conditions isn't met, the row is not possible. If you are not sure, the "I don't know" option is also a helpful answer.
        </p>
    </>),
    [DecisionPhase.ProvideReason]: (<>
        <p>
            Please evaluate each value in the second row.
        </p>
        <p>
            Start by clicking on the example value you want to evaluate.
            Then you can decide whether the value is possible or not.

            {/*
            TODO Backlogged.
            Then you can provide reasons why it should not be possible. If the column's value is possible in the given context, leave it as-is. If two or more columns are invalid because of how they interact with each other, please provide the reasons for all of them.
            */}
        </p>
        <p>
            When you are finished with the evaluation, please submit the results.
        </p>
    </>),
    [DecisionPhase.JustFinished]: (<>
        <p>
            Your answer was recorded. Please go back and continue with a next example.
        </p>
    </>),
    [DecisionPhase.Finished]: null,
};

type DecisionCardProps = {
    relation: ExampleRelation;
    selectedFDIndex: number;
};

function DecisionCard({ relation, selectedFDIndex }: DecisionCardProps) {
    const { decision } = useDecisionContext();

    return decision.phase === DecisionPhase.ProvideReason
        ? (<EditableDecisionCard relation={relation} selectedFDIndex={selectedFDIndex} />)
        : (<FinalDecisionCard relation={relation} selectedFDIndex={selectedFDIndex} />);
}

function EditableDecisionCard({ relation, selectedFDIndex }: DecisionCardProps) {
    const { exampleRow, columns } = relation;
    const { decision, setDecision } = useDecisionContext();
    const selected = decision.columns[selectedFDIndex];
    const isValid = selected.state === ColumnState.Valid;

    const updateColumn = useCallback((nextCol: Partial<DecisionColumn>) => {
        setDecision(prev => ({
            ...prev,
            columns: prev.columns.map((col, i) => i === selectedFDIndex ? { ...col, ...nextCol } : col),
        }));
    }, [ selectedFDIndex, setDecision ]);

    return (
        <Card>
            <CardHeader>
                <h3 className='font-semibold'>
                    Is the value
                    {' '}<span className='font-bold text-primary'>{exampleRow.values[selectedFDIndex]}</span>{' '}
                    in the column
                    {' '}<ColumnNameBadge name={columns[selectedFDIndex]} className='inline' />{' '}
                    possible?
                </h3>
            </CardHeader>

            <CardBody>
                <p className='mb-3'>
                    If the value is possible, the following functional dependency must be fake:
                </p>

                <FdDisplay relation={relation} selectedFDIndex={selectedFDIndex} />

                {selected.state === ColumnState.Undecided ? (
                    <div className='mt-6 flex gap-3'>
                        <Button color='success' onPress={() => updateColumn({ state: ColumnState.Valid })}>
                            Yes, it's possible!
                        </Button>

                        <Button color='danger' onPress={() => updateColumn({ state: ColumnState.Invalid })}>
                            No ...
                        </Button>
                    </div>
                ) : (
                    <div className='mt-6'>
                        <Switch
                            isSelected={isValid}
                            onValueChange={value => updateColumn({ state: value ? ColumnState.Valid : ColumnState.Invalid })}
                            classNames={{ label: 'text-base' }}
                        >
                            {isValid ? (<>
                                The example value is <span className='text-success'>possible</span>. The functional dependency is fake.
                            </>) : (<>
                                The example value is <span className='text-danger'>not possible</span>. The functional dependency is genuine.
                            </>)}
                        </Switch>

                        {/*
                        TODO Backlogged.
                        {!isValid && (<>
                            <p className='mt-6 mb-3'>
                                Please provide us with one or more reasons why the value
                                {' '}<span className='font-bold text-primary'>{exampleRow.values[selectedFDIndex]}</span>{' '}
                                is not possible. You can select from the predefined reasons or you can type your own.
                            </p>

                            <DecisionReasonsForm key={selected.colIndex} data={selected.reasons} setData={data => updateColumn({ ...selected, reasons: data })} />
                        </>)}
                        */}
                    </div>
                )}
            </CardBody>
        </Card>
    );
}

function FinalDecisionCard({ relation, selectedFDIndex }: DecisionCardProps) {
    const { exampleRow, columns } = relation;
    const { decision } = useDecisionContext();
    const selected = decision.columns[selectedFDIndex];
    const isValid = selected.state === ColumnState.Valid;

    return (
        <Card>
            <CardHeader>
                <h3 className='font-semibold'>
                    The value
                    {' '}<span className='font-bold text-primary'>{exampleRow.values[selectedFDIndex]}</span>{' '}
                    is
                    {' '}{isValid ? (
                        <span className='text-success'>possible</span>
                    ) : (
                        <span className='text-danger'>not possible</span>
                    )}{' '}
                    in the column
                    {' '}<ColumnNameBadge name={columns[selectedFDIndex]} className='inline' />
                    .
                </h3>
            </CardHeader>

            <CardBody>
                {/*
                    TODO Backlogged.
                    <DecisionReasonsOverview key={selected.colIndex} reasons={selected.reasons} />
                    */}

                <p className='mb-3'>
                    {isValid ? (<>
                        Therefore, the following functional dependency is fake:
                    </>) : (<>
                            Therefore, the following functional dependency is genuine:
                    </>)}
                </p>

                <FdDisplay relation={relation} selectedFDIndex={selectedFDIndex} />
            </CardBody>
        </Card>
    );
}

function FdDisplay({ relation: { exampleRow, columns }, selectedFDIndex }: DecisionCardProps) {
    const maxSetCols = exampleRow.maxSet.map(index => columns[index]);

    return (
        <div className='flex gap-4'>
            <div className='py-[2px] flex flex-wrap gap-x-2 gap-y-1'>
                {maxSetCols.map(col => (
                    <ColumnNameBadge key={col} name={col} className={exampleRow.isPositive ? 'bg-danger-400' : 'bg-warning-400'} />
                ))}
            </div>

            <FaArrowRight size={20} className='shrink-0' />

            <ColumnNameBadge name={columns[selectedFDIndex]} className='mt-[2px] shrink-0' />
        </div>
    );
}

type DecisionReasonsFormProps = {
    data: string[];
    setData: (data: string[]) => void;
};

function DecisionReasonsForm({ data, setData }: DecisionReasonsFormProps) {
    const { predefined, custom, customIndexes } = useMemo(() => ({
        predefined: data.filter(reason => predefinedReasons.includes(reason)),
        custom: data.filter(reason => !predefinedReasons.includes(reason)),
        customIndexes: data.map((_, i) => i).filter(i => !predefinedReasons.includes(data[i])),
    }), [ data ]);

    function updatePredefinedReason(reason: string, value: boolean) {
        setData(
            value
                ? [ ...data, reason ]
                : data.filter(r => r !== reason),
        );
    }

    function updateCustomReason(customIndex: number, reason: string | null) {
        const index = customIndexes[customIndex];
        setData(
            reason === null
                ? data.filter((_, i) => i !== index)
                : data.map((r, i) => i === index ? reason : r),
        );
    }

    return (
        <div className='grid grid-cols-2'>
            <div>
                {predefinedReasons.map(reason => (
                    <div key={reason} className='p-1 flex'>
                        <Checkbox isSelected={predefined.includes(reason)} className='gap-1' classNames={{ label: 'text-base/5' }} onValueChange={value => updatePredefinedReason(reason, value)}>
                            {reason}
                        </Checkbox>
                    </div>
                ))}
            </div>

            <div className='space-y-2'>
                {custom.map((reason, index) => (
                    <div key={index} className='flex items-center gap-2'>
                        <Button isIconOnly size='sm' variant='light' color='danger' className='' onPress={() => updateCustomReason(index, null)}>
                            <IoClose size={24} />
                        </Button>

                        <Input autoFocus size='sm' value={reason} onValueChange={value => updateCustomReason(index, value)} />
                    </div>
                ))}

                <div className=''>
                    <Button variant='light' color='primary' className='pl-1 pr-3 h-8 rounded-lg' onPress={() => setData([ ...data, '' ])}>
                        <IoAdd size={24} /><span>Add custom reason</span>
                    </Button>
                </div>
            </div>
        </div>
    );
}

const predefinedReasons = [
    'It would break the genuine functional dependency.',
    'It doesn\'t make sense in this domain.',
];

type DecisionReasonsOverviewProps = {
    reasons: string[];
};

function DecisionReasonsOverview({ reasons }: DecisionReasonsOverviewProps) {
    if (reasons.length === 0) {
        return (
            <p>
                There are no reasons provided.
            </p>
        );
    }

    return (
        <div>
            <p>
                {reasons.length === 1 ? (<>
                    The reason is:
                </>) : (<>
                    The reasons are:
                </>)}
            </p>

            {reasons.map(reason => (
                <div key={reason} className='flex items-center gap-2 font-bold'>
                    <TbPointFilled size={16} />
                    <span>{reason}</span>
                </div>
            ))}
        </div>
    );
}
