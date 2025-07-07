import { type Edge, MarkerType, type Node } from '@xyflow/react';
import { ColumnSet, type ColumnSetResponse } from './ColumnSet';

type FdNode = {
    id: string;
    label: string;
    columns: string[];
};

export type FdEdge = {
    readonly id: string;
    readonly source: FdNode;
    readonly target: FdNode;
};

export type RfNode = Node<{ label: string }>;
export type RfEdge = Edge;

export type RFGraph = {
    nodes: RfNode[];
    edges: RfEdge[];
};

export const NODE_OPTIONS: Omit<RfNode, 'id' | 'position' | 'data'> = {
    draggable: false,
    connectable: false,
    selectable: false,
    className: 'whitespace-pre-line',
};

export const EDGE_OPTIONS: Omit<RfEdge, 'id' | 'source' | 'target'> = {
    //animated: true,
    markerEnd: {
        width: 30,
        height: 30,
        type: MarkerType.Arrow,
    },
};

type FdResponse = {
    lhs: ColumnSetResponse;
    rhs: ColumnSetResponse;
};

type Fd = {
    lhs: ColumnSet;
    rhs: ColumnSet;
};

export type FdSetResponse = {
    columns: string[];
    fds: FdResponse[];
};

export class FdSet {
    private constructor(
        /** Names of the columns. They are expected to be unique. */
        readonly columns: string[],
        /**
         * For each column index i, there is a list of all column sets that when put on the lhs they form a functional dependency (with i on the rhs).
         * All numbers are indexes to the {@link FdSet::columns} array.
         */
        readonly fds: Fd[],
    ) {}

    static fromResponse(input: FdSetResponse): FdSet {
        const columns = input.columns;
        const fds = input.fds.map(fd => ({
            lhs: ColumnSet.fromResponse(fd.lhs),
            rhs: ColumnSet.fromResponse(fd.rhs),
        }));

        return new FdSet(columns, fds);
    }
}
