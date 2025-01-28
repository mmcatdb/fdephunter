import { type ReactNode, useCallback, useMemo, useState } from 'react';
import { ColumnState, type DecisionColumn, DecisionPhase, useDecisionContext } from '@/context/DecisionProvider';
import { DecisionStatus } from '@/types/decision';
import { useNavigate } from 'react-router';
import { IoAdd, IoClose } from 'react-icons/io5';
import { routes } from '@/router';
import { AssignmentVerdictLabel } from './AssignmentVerdictLabel';
import { AssignmentVerdict, type Assignment } from '@/types/assignment';
import { API } from '@/utils/api';
import { TbPointFilled } from 'react-icons/tb';
import { Button, Card, CardBody, CardFooter, CardHeader, Checkbox, Input, Switch } from '@nextui-org/react';
import { ExampleRelationDisplay } from './dataset/ArmstrongRelationDisplay';
import { type ExampleRelation } from '@/types/armstrongRelation';
import { ColumnNameBadge } from './dataset/FDListDisplay';
import { FaArrowRight } from 'react-icons/fa';

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
            <ControlCard assignment={assignment} onEvaluated={onEvaluated} />

            <Card className='p-4 max-w-full'>
                <ExampleRelationDisplay
                    relation={assignment.exampleRelation}
                    selectedColIndex={selectedFDIndex}
                    setSelectedColIndex={areColsClickable ? setSelectedFDIndex : undefined}
                />
            </Card>

            {areColsClickable && (
                <DecisionReasonsCard relation={assignment.exampleRelation} selectedFDIndex={selectedFDIndex} />
            )}
        </div>
    );
}

type ControlCardProps = {
    assignment: Assignment;
    onEvaluated: (assignment: Assignment) => void;
};

function ControlCard({ assignment, onEvaluated }: ControlCardProps) {
    const { decision, setDecision } = useDecisionContext();
    const [ fetching, setFetching ] = useState(false);
    const navigate = useNavigate();

    async function evaluate(status: DecisionStatus) {
        const columns = status === DecisionStatus.Rejected
            ? decision.columns.map(col => {
                const trimmed = col.reasons.map(reason => reason.trim()).filter(reason => reason.length > 0);
                const unique = [ ...new Set(trimmed) ];

                return {
                    name: col.name,
                    reasons: unique,
                };
            })
            : [];

        setFetching(true);
        const response = await API.assignments.evaluate({ assignmentId: assignment.id }, {
            status,
            columns,
        });
        setFetching(false);
        if (!response.status)
            return;

        setDecision({ ...decision, phase: DecisionPhase.JustFinished });
        onEvaluated(assignment);
    }

    function continueAccepted() {
        void navigate(routes.worker.detail.resolve({ workerId: assignment.workerId }));
    }

    const isUndecided = decision.phase !== DecisionPhase.Finished && decision.columns.some(column => column.state === ColumnState.Undecided);

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
                    <div>
                        This example was evaluated as <AssignmentVerdictLabel verdict={assignment.verdict} />.
                    </div>
                )}
            </CardBody>

            {decision.phase !== DecisionPhase.Finished && (
                <CardFooter className='gap-3'>
                    {decision.phase === DecisionPhase.AnswerYesNo && (<>
                        <Button color='success' onPress={() => evaluate(DecisionStatus.Accepted)} isLoading={fetching}>
                            Yes, the example is correct
                        </Button>
                        <Button color='danger' onPress={() => setDecision({ ...decision, phase: DecisionPhase.ProvideReason })}>
                            No, the example is incorrect
                        </Button>
                        <Button color='warning' onPress={() => evaluate(DecisionStatus.Unanswered)} isLoading={fetching}>
                            {`I don't know ...`}
                        </Button>
                    </>)}

                    {decision.phase === DecisionPhase.ProvideReason && (<>
                        <Button color='primary' onPress={() => evaluate(DecisionStatus.Rejected)} isLoading={fetching} isDisabled={isUndecided}>
                            Submit
                        </Button>
                        <Button color='warning' onPress={() => evaluate(DecisionStatus.Unanswered)} isLoading={fetching}>
                            {`I don't know ...`}
                        </Button>
                    </>)}

                    {decision.phase === DecisionPhase.JustFinished && (<>
                        <Button color='primary' onPress={continueAccepted}>
                            Go back and continue
                        </Button>
                    </>)}
                </CardFooter>
            )}
        </Card>
    );
}

const titles: { [key in DecisionPhase]: string } = {
    [DecisionPhase.AnswerYesNo]: 'Is this example correct?',
    [DecisionPhase.ProvideReason]: 'Why is this example incorrect?',
    [DecisionPhase.JustFinished]: 'Thank you!',
    [DecisionPhase.Finished]: 'Assignment finished',
};

const bodies: { [key in DecisionPhase]: ReactNode } = {
    [DecisionPhase.AnswerYesNo]: (<>
        <p>
            The first row is from the original dataset. The second one was generated. Please decide if the second row is a valid part of the dataset.
        </p>
        <p>
            The row should be marked as possible only if all its values are valid as well as all their combinations (with regards to all other values in the first row). If any of these conditions {`isn't`} met, the row is invalid. If you are not sure, the {`"I don't know"`} option is also a helpful answer.
        </p>
    </>),
    [DecisionPhase.ProvideReason]: (<>
        <p>
            Please evaluate each value in the second row.
        </p>
        <p>
            Start by clicking on the example value you want to evaluate. Then you can provide reasons why it should be a negative example. If the {`column's`} value is possible in the given context, leave it as-is. If two or more columns are invalid because of how they interact with each other, please provide the reasons for all of them.
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

type DecisionReasonsCardProps = {
    relation: ExampleRelation;
    selectedFDIndex: number | undefined;
};

function DecisionReasonsCard({ relation: { exampleRow, columns }, selectedFDIndex }: DecisionReasonsCardProps) {
    const { decision, setDecision } = useDecisionContext();
    const selected = selectedFDIndex !== undefined && decision.columns[selectedFDIndex];
    const isEditable = decision.phase === DecisionPhase.ProvideReason;

    const updateColumn = useCallback((nextCol: Partial<DecisionColumn>) => {
        setDecision(prev => ({
            ...prev,
            columns: prev.columns.map((col, i) => i === selectedFDIndex ? { ...col, ...nextCol } : col),
        }));
    }, [ selectedFDIndex, setDecision ]);

    if (!selected)
        return null;

    const isValid = isEditable
        ? selected.state === ColumnState.Valid
        : selected.reasons.length === 0;

    const maxSetCols = exampleRow.maxSet.map(index => columns[index]);

    return (
        <Card>
            <CardHeader>
                <h3 className='font-semibold'>
                    {isEditable ? (<>
                        Is this functional dependency valid?
                    </>) : (<>
                        This functional dependency is <span className={isValid ? 'text-success' : 'text-danger'}>{isValid ? 'valid' : 'invalid'}</span>
                    </>)}
                </h3>
            </CardHeader>

            <CardBody className='space-y-6'>
                <div className='flex gap-4'>
                    <div className='py-[2px] flex flex-wrap gap-x-2 gap-y-1'>
                        {maxSetCols.map(col => (
                            <ColumnNameBadge key={col} name={col} className={exampleRow.isNegative ? 'bg-warning-400' : 'bg-danger-400'} />
                        ))}
                    </div>

                    <FaArrowRight size={20} className='shrink-0' />

                    <ColumnNameBadge name={columns[selectedFDIndex]} className='mt-[2px] shrink-0' />
                </div>

                {isEditable ? (<>
                    {selected.state === ColumnState.Undecided ? (
                        <div className='flex gap-3'>
                            <Button color='success' onPress={() => updateColumn({ state: ColumnState.Valid })}>
                                Yes!
                            </Button>

                            <Button color='danger' onPress={() => updateColumn({ state: ColumnState.Invalid })}>
                                No ...
                            </Button>
                        </div>
                    ) : (
                        <div className='space-y-6'>
                            <Switch
                                isSelected={isValid}
                                onValueChange={value => updateColumn({ state: value ? ColumnState.Valid : ColumnState.Invalid })}
                                classNames={{ label: 'text-base' }}
                            >
                                it is <span className={isValid ? 'text-success' : 'text-danger'}>{isValid ? 'valid' : 'invalid'}</span>
                            </Switch>

                            {!isValid && (<>
                                <p>
                                    Please provide us with one or multiple reasons why the value <span className='font-bold text-primary'>{exampleRow.values[selectedFDIndex]}</span> {`isn't`} valid. You can select from the predefined reasons or you can type your own.
                                </p>

                                <DecisionReasonsForm key={selected.colIndex} data={selected.reasons} setData={data => updateColumn({ ...selected, reasons: data })} />
                            </>)}
                        </div>
                    )}
                </>) : !isValid ? (<>
                    <p>
                        You can see the reasons here:
                    </p>
                    <DecisionReasonsOverview key={selected.colIndex} data={selected.reasons} />
                </>) : null}
            </CardBody>
        </Card>
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
    'VALUE_MUST_BE_UNIQUE_IN_COLUMNS',
    'VALUE_MUST_BE_UNIQUE_IN_ROW',
    'VALUES_INDETIFY_EACH_OTHER',
    'VALUES_DO_NOT_MATCH',
    'VALUE_MUST_BE_IN_RANGE',
    'VALUE_DOES_NOT_MAKE_SENSE_AT_ALL',
];

type DecisionReasonsOverviewProps = {
    data: string[];
};

function DecisionReasonsOverview({ data }: DecisionReasonsOverviewProps) {
    return (<>
        {data.values().map(reason => (
            <div key={reason} className='min-h-10 flex items-center'>
                <span className='flex items-center font-bold'>
                    <TbPointFilled size={16} />
                    <span>{reason}</span>
                </span>
            </div>
        ))}
    </>);
}
