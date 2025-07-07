import { createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useState } from 'react';
import { type DecisionColumn, DecisionColumnStatus, type ExampleDecision } from '@/types/examples';
import { type Assignment } from '@/types/assignment';

export enum DecisionPhase {
    Evaluation = 'EVALUATION',
    JustFinished = 'JUSTFINISHED',
    Finished = 'FINISHED',
}

type DecisionState = {
    phase: DecisionPhase;
    columns: DecisionColumn[];
};

type DecisionContext = {
    decision: DecisionState;
    setDecision: Dispatch<SetStateAction<DecisionState>>;
};

const decisionContext = createContext<DecisionContext | undefined>(undefined);

type DecisionProviderProps = {
    children: ReactNode;
    assignment: Assignment;
    inputDecision: ExampleDecision | undefined;
};

export function DecisionProvider({ children, assignment, inputDecision }: DecisionProviderProps) {
    const [ decision, setDecision ] = useState<DecisionState>(createDefaultDecision(assignment, inputDecision));

    return (
        <decisionContext.Provider value={{ decision, setDecision }}>
            { children }
        </decisionContext.Provider>
    );
}

export function useDecisionContext(): DecisionContext {
    const context = useContext(decisionContext);
    if (context === undefined)
        throw new Error('useContext must be used within an DecisionProvider');

    return context;
}

function createDefaultDecision(assignment: Assignment, inputDecision: ExampleDecision | undefined): DecisionState {
    const columns = inputDecision
        ? assignment.columns.map((_, colIndex) => ({
            status: inputDecision.columns[colIndex].status,
            reasons: inputDecision.columns[colIndex].reasons,
        }))
        : assignment.columns.map((_, colIndex) => ({
            status: assignment.exampleRow.rhsSet.has(colIndex) ? DecisionColumnStatus.Undecided : undefined,
            reasons: [],
        }));

    return {
        phase: inputDecision ? DecisionPhase.Finished : DecisionPhase.Evaluation,
        columns,
    };
}
