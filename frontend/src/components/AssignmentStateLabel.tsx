import { AssignmentState } from '@/types/assignment';

type AssignmentStateLabelProps = {
    status: AssignmentState;
};

export function AssignmentStateLabel({ status }: AssignmentStateLabelProps) {
    return (
        <span className={statusDescriptions[status].color}>
            {statusDescriptions[status].label}
        </span>
    );
}

const statusDescriptions: {
    [key in AssignmentState]: {
        color: string;
        label: string;
    };
} = {
    [AssignmentState.New]: { color: 'text-primary', label: 'not evaluated' },
    [AssignmentState.Accepted]: { color: 'text-success', label: 'possible' },
    [AssignmentState.Rejected]: { color: 'text-danger', label: 'not possible' },
    [AssignmentState.DontKnow]: { color: 'text-warning', label: 'uncertain' },
};
