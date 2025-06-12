import { createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useState } from 'react';
import { type ExampleRelation } from '@/types/armstrongRelation';
import { type ExampleDecision, DecisionColumnStatus } from '@/types/assignment';

export type DecisionColumn = {
    colIndex: number;
    name: string;
    status: DecisionColumnStatus | undefined;
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
    inputDecision: ExampleDecision | undefined;
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

function createDefaultDecision(relation: ExampleRelation, inputDecision: ExampleDecision | undefined): DecisionState {
    const columns = inputDecision
        ? relation.columns.map((name, colIndex) => ({
            colIndex,
            name,
            status: inputDecision.columns[colIndex].status,
            reasons: inputDecision.columns[colIndex].reasons,
        }))
        : relation.columns.map((name, colIndex) => ({
            colIndex,
            name,
            status: relation.exampleRow.maxSet.includes(colIndex) ? undefined : DecisionColumnStatus.Undecided,
            reasons: [],
        }));

    return {
        phase: inputDecision ? DecisionPhase.Finished : DecisionPhase.Evaluation,
        columns,
    };
}
