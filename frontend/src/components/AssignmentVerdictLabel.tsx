import { AssignmentVerdict } from '@/types/assignment';

type AssignmentVerdictLabelProps = {
    verdict: AssignmentVerdict;
}

export default function AssignmentVerdictLabel({ verdict }: AssignmentVerdictLabelProps) {
    return (
        <span className={`text-${verdictDescriptions[verdict].color}`}>
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
    [AssignmentVerdict.New]: { color: 'info', label: 'not evaluated' },
    [AssignmentVerdict.Accepted]: { color: 'success', label: 'positive' },
    [AssignmentVerdict.Rejected]: { color: 'danger', label: 'negative' },
    [AssignmentVerdict.DontKnow]: { color: 'warning', label: 'uncertain' },
};