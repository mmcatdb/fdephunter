import { AssignmentVerdict } from '@/types/assignment';

type AssignmentVerdictLabelProps = {
    verdict: AssignmentVerdict;
};

export function AssignmentVerdictLabel({ verdict }: AssignmentVerdictLabelProps) {
    return (
        <span className={verdictDescriptions[verdict].color}>
            {verdictDescriptions[verdict].label}
        </span>
    );
}

const verdictDescriptions: {
    [key in AssignmentVerdict]: {
        color: string;
        label: string;
    };
} = {
    [AssignmentVerdict.New]: { color: 'text-primary', label: 'not evaluated' },
    [AssignmentVerdict.Accepted]: { color: 'text-success', label: 'possible' },
    [AssignmentVerdict.Rejected]: { color: 'text-danger', label: 'not possible' },
    [AssignmentVerdict.DontKnow]: { color: 'text-warning', label: 'uncertain' },
};
