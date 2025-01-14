import React, { createContext, type ReactNode, useContext, useState } from 'react';
import { type DatasetDataWithExamples } from '@/types/dataset';

export type DecisionColumn = {
    id: string;
    index: number;
    name: string;
    value: string;
    reasons: string[];
};
    
export type DecisionRow = {
    index: number;
    columns: DecisionColumn[];
};

export enum DecisionPhase {
    AnswerYesNo = 'answerYesNo',
    ProvideReason = 'provideReason',
    JustFinished = 'justFinished',
    Finished = 'finished',
}

export type DecisionState = {
    phase: DecisionPhase;
    selectedColumn?: {
        rowIndex: number;
        colIndex: number;
    };
    rows: DecisionRow[];
};

export type DecisionContext = {
    decision: DecisionState;
    setDecision: React.Dispatch<React.SetStateAction<DecisionState>>;
};

const decisionContext = createContext<DecisionContext | undefined>(undefined);

interface DecisionProviderProps {
    children: ReactNode;
    data: DatasetDataWithExamples;
    isFinished: boolean;
}

export function DecisionProvider({ children, data, isFinished }: DecisionProviderProps) {
    const [ decision, setDecision ] = useState<DecisionState>(createDefaultDecision(data, isFinished));

    return (
        <decisionContext.Provider value={{ decision, setDecision }}>
            { children }
        </decisionContext.Provider>
    );
}

export default function useDecisionContext(): DecisionContext {
    const context = useContext(decisionContext);
    if (context === undefined)
        throw new Error('useContext must be used within an DecisionProvider');

    return context;
}

function createDefaultDecision(data: DatasetDataWithExamples, isFinished: boolean): DecisionState {
    const rows = (data.examples ?? []).map((row, rowIndex) => {
        const columns = row.map((column, colIndex) => ({
            id: `${rowIndex}_${colIndex}`,
            index: colIndex,
            name: data.header[colIndex],
            value: column,
            reasons: [] as string[],
        }));

        return {
            index: rowIndex,
            columns,
        };
    });

    return {
        phase: isFinished ? DecisionPhase.Finished : DecisionPhase.AnswerYesNo,
        rows,
    };
}