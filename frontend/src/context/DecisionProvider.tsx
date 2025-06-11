import { createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useState } from 'react';
import { type ExampleRelation } from '@/types/armstrongRelation';
import { type AssignmentDecision, DecisionColumnStatus } from '@/types/assignment';

export type DecisionColumn = {
    colIndex: number;
    name: string;
    status?: DecisionColumnStatus;
    reasons: string[];
};

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
    relation: ExampleRelation;
    inputDecision: AssignmentDecision | undefined;
};

export function DecisionProvider({ children, relation, inputDecision }: DecisionProviderProps) {
    const [ decision, setDecision ] = useState<DecisionState>(createDefaultDecision(relation, inputDecision));

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

function createDefaultDecision(relation: ExampleRelation, inputDecision: AssignmentDecision | undefined): DecisionState {
    const columns = inputDecision
        ? relation.columns.map((name, colIndex) => ({
            colIndex,
            name,
            state: inputDecision.columns[colIndex].status,
            reasons: inputDecision.columns[colIndex].reasons,
        }))
        : relation.columns.map((name, colIndex) => ({
            colIndex,
            name,
            state: relation.exampleRow.maxSet.includes(colIndex) ? undefined : DecisionColumnStatus.Undecided,
            reasons: [],
        }));

    return {
        phase: inputDecision ? DecisionPhase.Finished : DecisionPhase.Evaluation,
        columns,
    };
}
