import { createContext, type Dispatch, type ReactNode, type SetStateAction, useContext, useState } from 'react';
import { type ExampleRelation } from '@/types/armstrongRelation';

export enum ColumnState {
    Undecided = 'undecided',
    Valid = 'valid',
    Invalid = 'invalid',
}

export type DecisionColumn = {
    colIndex: number;
    name: string;
    state?: ColumnState;
    reasons: string[];
};

export enum DecisionPhase {
    AnswerYesNo = 'answerYesNo',
    ProvideReason = 'provideReason',
    JustFinished = 'justFinished',
    Finished = 'finished',
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
    isFinished: boolean;
};

export function DecisionProvider({ children, relation, isFinished }: DecisionProviderProps) {
    const [ decision, setDecision ] = useState<DecisionState>(createDefaultDecision(relation, isFinished));

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

function createDefaultDecision(relation: ExampleRelation, isFinished: boolean): DecisionState {
    console.log({ relation });
    const columns = relation.columns.map((name, colIndex) => ({
        colIndex,
        name,
        state: relation.exampleRow.maximalSet.includes(colIndex) ? undefined : ColumnState.Undecided,
        reasons: [],
    }));

    return {
        phase: isFinished ? DecisionPhase.Finished : DecisionPhase.AnswerYesNo,
        columns,
    };
}
