import { DecisionPhase, useDecisionContext } from '@/context/DecisionProvider';
import { DecisionStatus } from '@/types/decision';
import { type ReactNode, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TiPlus } from 'react-icons/ti';
import { IoClose } from 'react-icons/io5';
import { routes } from '@/router';
import { AssignmentVerdictLabel } from './AssignmentVerdictLabel';
import { type Assignment } from '@/types/assignment';
import { API } from '@/utils/api';
import { TbPointFilled } from 'react-icons/tb';
import { Button, Card, CardBody, CardFooter, CardHeader } from '@nextui-org/react';

type AssignmentEvaluationProps = {
    assignment: Assignment;
    onEvaluated: (assignment: Assignment) => void;
};

export function AssignmentEvaluation({ assignment, onEvaluated }: AssignmentEvaluationProps) {
    return (
        <div className='grid grid-cols-10 gap-4'>
            <div className='col-span-10 lg:col-span-10 xl:col-span-6'>
                <ControlCard
                    assignment={assignment}
                    onEvaluated={onEvaluated}
                />
            </div>

            <div className='col-span-10 lg:col-span-10 xl:col-span-6'>
                <DecisionReasonsCard />
            </div>
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
        const filteredColumns = decision.rows[0].columns.filter(column => column.reasons.length > 0);
        // If the user select Rejected but then he don't provide any reasons, we evaluate as Accepted.
        if (status === DecisionStatus.Rejected && filteredColumns.length === 0)
            status = DecisionStatus.Accepted;

        const columns = status === DecisionStatus.Rejected
            ? filteredColumns.map(column => ({ name: column.name, reasons: column.reasons }))
            : [];

        setFetching(true);
        const response = await API.assignments.evaluate({ assignmentId: assignment.id }, {
            status,
            columns,
        });
        setFetching(false);
        if (!response.status)
            return;

        setDecision({ ...decision, phase: DecisionPhase.JustFinished, selectedColumn: undefined });
        onEvaluated(assignment);
    }

    function continueAccepted() {
        navigate(routes.worker.detail.resolve({ workerId: assignment.workerId }));
    }

    return (
        <Card>
            <CardHeader>
                <h3 className='font-semibold'>
                    {titles[decision.phase]}
                </h3>
            </CardHeader>
            <CardBody>
                {bodies[decision.phase]}
                {decision.phase === DecisionPhase.Finished && (<>
                    This example was evaluated as <AssignmentVerdictLabel verdict={assignment.verdict} />
                </>)}
            </CardBody>
            {decision.phase !== DecisionPhase.Finished && (
                <CardFooter>
                    {decision.phase === DecisionPhase.AnswerYesNo && (<>
                        <Button
                            color='success'
                            onPress={() => evaluate(DecisionStatus.Accepted)}
                            isLoading={fetching}
                        >
                            Yes, the row is possible
                        </Button>
                        <Button color='danger' className='ml-4' onPress={() => setDecision({ ...decision, phase: DecisionPhase.ProvideReason })}>
                            No, the row is invalid
                        </Button>
                        <Button
                            className='ml-4'
                            color='warning'
                            onPress={() => evaluate(DecisionStatus.Unanswered)}
                            isLoading={fetching}
                        >
                            {`I don't know ...`}
                        </Button>
                    </>)}
                    {decision.phase === DecisionPhase.ProvideReason && (<>
                        <Button
                            color='primary'
                            onPress={() => evaluate(DecisionStatus.Rejected)}
                            isLoading={fetching}
                        >
                            Submit
                        </Button>
                        <Button
                            className='ml-4'
                            color='warning'
                            onPress={() => evaluate(DecisionStatus.Unanswered)}
                            isLoading={fetching}
                        >
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
    [DecisionPhase.AnswerYesNo]: 'Can the dataset include the last row?',
    [DecisionPhase.ProvideReason]: 'Evaluate each column individually',
    [DecisionPhase.JustFinished]: 'Thank you!',
    [DecisionPhase.Finished]: 'Assignment finished',
};

const bodies: { [key in DecisionPhase]: ReactNode } = {
    [DecisionPhase.AnswerYesNo]: (<>
        <p>
            It was generated as a negative example. But is it really a negative example? Please help us decide if the row would be a valid part of the dataset.
        </p>
        The row should be marked as possible only if all its values are valid as well as all their combinations (with regards to all other values in the dataset). If any of these conditions {`isn't`} met, the row is invalid. If you are not sure, the {`"I don't know"`} is also a helpful answer.
    </>),
    [DecisionPhase.ProvideReason]: (<>
        <p>
            Start by clicking on the example value you want to evaluate. Then you can provide reasons why it should be a negative example. If the {`column's`} value is possible in the given context, leave it as-is. If two or more columns are invalid because of how they interact with each other, please provide the reasons for all of them.
        </p>
        When you are finished with the evaluation, please submit the results.
    </>),
    [DecisionPhase.JustFinished]: (<>
        Your answer was recorded. Please go back and continue with a next example.
    </>),
    [DecisionPhase.Finished]: null,
};

function DecisionReasonsCard() {
    const { decision, setDecision } = useDecisionContext();
    const selectedColumn = decision.selectedColumn && decision.rows[decision.selectedColumn.rowIndex].columns[decision.selectedColumn.colIndex];
    const isEditable = decision.phase === DecisionPhase.ProvideReason;

    function setData(reasons: string[]) {
        if (!selectedColumn)
            return;

        selectedColumn.reasons = reasons;
        setDecision({ ...decision });
    }

    if (!selectedColumn)
        return null;

    const isNegative = selectedColumn.reasons.length !== 0;

    if (!isEditable && !isNegative)
        return null;

    return (
        <Card>
            <CardHeader>
                <h3 className='font-semibold'>
                    {isEditable ? (<>
                        Why is this column a negative example?
                    </>) : (<>
                        This column is {isNegative ? 'negative' : 'positive'}
                    </>)}
                </h3>
            </CardHeader>
            <CardBody>
                {isEditable ? (<>
                    <p>
                        Please provide us with one or multiple reasons why the value <span className='font-bold text-primary'>{selectedColumn.value}</span> {`isn't`} valid. You can select from the predefined reasons or you can type your own.
                    </p>
                    <DecisionReasonsForm key={selectedColumn.id} data={selectedColumn.reasons} setData={setData} />
                </>) : (<>
                    <p>
                        You can se the reasons here:
                    </p>
                    <DecisionReasonsOverview key={selectedColumn.id} data={selectedColumn.reasons} />
                </>)}
            </CardBody>
        </Card>
    );
}

type DecisionReasonsFormProps = {
    data: string[];
    setData: (data: string[]) => void;
};

function DecisionReasonsForm({ data, setData }: DecisionReasonsFormProps) {
    const [ editingIndex, setEditingIndex ] = useState<number | undefined>(data.length === 0 ? 0 : undefined);
    const [ isAdding, setIsAdding ] = useState(data.length === 0);
    const innerData = isAdding ? [ ...data, '' ] : [ ...data ];

    function finishEditingReason(newValue: string) {
        if (editingIndex === undefined)
            return;

        if (!newValue && data.length === 0)
            return;

        setEditingIndex(undefined);
        setIsAdding(false);

        if (!newValue)
            return;

        innerData[editingIndex] = newValue;
        setData(innerData.filter(reason => !!reason));
    }

    function startEditingReason(index: number) {
        setEditingIndex(index);
        setIsAdding(false);
    }

    function addReason() {
        setIsAdding(true);
        setEditingIndex(data.length);
    }

    function deleteReason(index: number) {
        setEditingIndex(undefined);
        setIsAdding(false);
        setData(data.filter((_, i) => i !== index));
    }

    return (<>
        {innerData.map((reason, index) => (
            <div key={index}>
                {index === editingIndex ? (
                    <ReasonSelect value={innerData[editingIndex]} onChange={finishEditingReason}/>
                ) : (
                    <div className='min-h-10 flex items-center'>
                        <span className='flex items-center font-bold'>
                            {/* TODO Replace by button. */}
                            <IoClose
                                size={24}
                                className='cursor-pointer text-danger mr-2'
                                onClick={() => deleteReason(index)}
                            />
                            {/* TODO Replace by button. */}
                            <span className='cursor-pointer' onClick={() => startEditingReason(index)}>{reason}</span>
                        </span>
                    </div>
                )}
            </div>
        ))}
        {!isAdding && (
            <div className='mt-2'>
                {/* TODO Replace by button. */}
                <span className='flex items-center cursor-pointer text-primary' onClick={addReason}>
                    <TiPlus size={20} className='mr-2' /><span>Add reason</span>
                </span>
            </div>
        )}
    </>);
}

type ReasonSelectProps = {
    value: string;
    onChange: (value: string) => void;
};

function ReasonSelect({ value, onChange }: ReasonSelectProps) {
    const options = useMemo(() => (!value || predefinedReasons.includes(value)) ? predefinedOptions : [ ...predefinedOptions, valueToOption(value) ], [ value ]);

    // const handleChange = useCallback((option: SingleValue<Option>) => {
    //     onChange(option ? option.value : '');
    // }, [ onChange ]);

    return (
    // FIXME Replace with Autocomplete, or some entirely different ux.

        // <CreatableSelect
        //     openMenuOnFocus
        //     options={options}
        //     value={value ? valueToOption(value) : undefined}
        //     onChange={handleChange}
        //     onBlur={e => onChange(e.target.value)}
        //     isValidNewOption={inputValue => !!inputValue}
        //     placeholder='Select an option or provide a custom one ...'
        // />
        <div></div>
    );
}

type Option = {
    value: string;
    label: string;
};

function valueToOption(value: string): Option {
    return {
        value,
        label: value,
    };
}

const predefinedReasons = [
    'VALUE_MUST_BE_UNIQUE_IN_COLUMNS',
    'VALUE_MUST_BE_UNIQUE_IN_ROW',
    'VALUES_INDETIFY_EACH_OTHER',
    'VALUES_DO_NOT_MATCH',
    'VALUE_MUST_BE_IN_RANGE',
    'VALUE_DOES_NOT_MAKE_SENSE_AT_ALL',
];

const predefinedOptions = predefinedReasons.map(valueToOption);

type DecisionReasonsOverviewProps = {
    data: string[];
};

function DecisionReasonsOverview({ data }: DecisionReasonsOverviewProps) {
    return (<>
        {data.map((reason, index) => (
            <div key={index} className='min-h-10 flex items-center'>
                <span className='flex items-center font-bold'>
                    <TbPointFilled size={16} />
                    <span>{reason}</span>
                </span>
            </div>
        ))}
    </>);
}
