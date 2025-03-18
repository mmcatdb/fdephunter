import { memo, useMemo, useState } from 'react';
import { NODE_OPTIONS, type RFGraph, type RFNode, type RFEdge } from '@/types/FD';
import { Handle, type NodeProps, Position, ReactFlow } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { computeColumnIndexesForLatticeRow, computeEdgesForLatticeCell, type Lattice, McType } from '@/types/armstrongRelation';
import { Button, cn, Divider, Select, SelectItem, type SharedSelection, Switch } from '@nextui-org/react';
import clsx from 'clsx';

export function LatticeDisplay({ lattices }: { lattices: Lattice[] }) {
    const [ classIndex, setClassIndex ] = useState(new Set([ '0' ]));
    const classItems = useMemo(() => lattices.map((lattice, index) => ({ value: index, label: lattice.class })), [ lattices ]);
    const lattice = lattices[Number(classIndex.values().next().value!)];

    const [ showAllRows, setShowAllRows ] = useState(true);
    const [ rowIndex, setRowIndex ] = useState(0);

    const selectedIndex = showAllRows ? undefined : rowIndex;
    const rfGraph = useMemo(() => createLatticeGraph(lattice, selectedIndex), [ lattice, selectedIndex ]);

    return (
        <div className='w-full h-full flex flex-col'>
            <div className={clsx('h-10 flex items-center justify-center gap-8', !showAllRows && 'invisible')}>
                <Select
                    label='Class'
                    items={classItems}
                    selectedKeys={classIndex}
                    disallowEmptySelection
                    onSelectionChange={setClassIndex as (keys: SharedSelection) => void}
                    className='visible max-w-sm'
                >
                    {item => (
                        <SelectItem key={item.value}>{item.label}</SelectItem>
                    )}
                </Select>

                <Switch isSelected={showAllRows} onValueChange={setShowAllRows} className='visible'>
                    Show all rows?
                </Switch>

                <Divider orientation='vertical' />

                <div>
                    Row:{' '}
                    <span className='tabular-nums text-lg font-bold'>
                        {rowIndex + 1}
                    </span>
                </div>

                <div className='space-x-2'>
                    <Button onPress={() => setRowIndex(prev => Math.max(0, prev - 1))} isDisabled={!showAllRows || rowIndex === 0}>
                        Previous
                    </Button>

                    <Button onPress={() => setRowIndex(prev => Math.min(lattice.rows.length - 1, prev + 1))} isDisabled={!showAllRows || rowIndex === lattice.rows.length - 1}>
                        Next
                    </Button>
                </div>
            </div>
            <div className='w-full h-full'>
                <ReactFlow
                    fitView
                    nodeTypes={nodeTypes}
                    nodes={rfGraph.nodes}
                    edges={rfGraph.edges}
                />
            </div>
        </div>
    );
}

/** If the row index is undefined, we want to create the whole graph. */
function createLatticeGraph(lattice: Lattice, rowIndex: number | undefined): RFGraph {
    if (rowIndex !== undefined) {
        const columnIndexes = computeColumnIndexesForLatticeRow(rowIndex, lattice.columns.length);
        return {
            nodes: createRowNodes(lattice, rowIndex, columnIndexes, 0, 0),
            edges: [],
        };
    }

    const widestRow = Math.floor((lattice.columns.length - 1) / 2);
    const maxWidth = getRowWidth(widestRow, lattice.columns.length);

    const nodes: RFNode[] = [];
    const edges: RFEdge[] = [];

    let yOffset = 0;
    for (let i = lattice.rows.length - 1; i >= 0; i--) {
        const width = getRowWidth(i, lattice.columns.length);
        const xOffset = (maxWidth - width) / 2;

        const columnIndexes = computeColumnIndexesForLatticeRow(i, lattice.columns.length);
        nodes.push(...createRowNodes(lattice, i, columnIndexes, xOffset, yOffset));
        edges.push(...createRowEdges(lattice, columnIndexes));

        yOffset += getRowHeight(i) + NODE_Y_GAP;
    }

    return {
        nodes,
        edges,
    };
}

function createRowNodes(lattice: Lattice, rowIndex: number, columnIndexes: number[][], xOffset: number, yOffset: number): RFNode[] {
    const types = lattice.rows[rowIndex].cells;

    return columnIndexes.map((columnIndexes, cellIndex) => {
        const id = computeNodeId(columnIndexes);
        const type = types[cellIndex];
        const label = columnIndexes.map(i => lattice.columns[i]).join('\n');

        return {
            id,
            type: 'NodeComponent',
            position: {
                x: xOffset + (NODE_SIZE + NODE_X_GAP) * cellIndex,
                y: yOffset,
            },
            data: {
                label,
            },
            ...NODE_OPTIONS,
            className: cn(
                NODE_OPTIONS.className,
                '!w-[120px] !px-1 !py-0 !border-4 !text-black text-center rounded text-sm',
                bold.includes(type) && 'font-bold',
                red.includes(type) && '!border-red-600 !bg-red-300',
                blue.includes(type) && '!border-blue-600 !bg-blue-300',
                yellow.includes(type) && '!border-yellow-600 !bg-yellow-200',
                white.includes(type) && '!bg-white',
            ),
        } satisfies RFNode;
    });
}

function computeNodeId(cell: number[]): string {
    return cell.join('-');
}

function createRowEdges(lattice: Lattice, columnIndexes: number[][]): RFEdge[] {
    return columnIndexes.flatMap(cell => {
        const source = computeNodeId(cell);
        const edgesForCells = computeEdgesForLatticeCell(cell, lattice.columns.length);

        return edgesForCells.map(edge => {
            const target = computeNodeId(edge);

            return {
                id: `${source}_${target}`,
                source,
                target,
                // ...EDGE_OPTIONS,
                sourceHandle: 'source',
                targetHandle: 'target',
                type: 'straight',
                selectable: false,
            };
        });
    });
}

const bold = [ McType.Final, McType.Genuine, McType.Eliminated ];
const white = [ McType.Subset, McType.Derived, McType.Coincidental ];

const red = [ McType.Final, McType.Initial, McType.Subset ];
const blue = [ McType.Genuine, McType.Candidate, McType.Derived ];
const yellow = [ McType.Eliminated, McType.Targeted, McType.Coincidental ];

/** In px. */
const NODE_SIZE = 120;
/** In px. */
const NODE_X_GAP = 20;
/** In px. */
const NODE_Y_GAP = 40;

/** In px. */
function getRowHeight(rowIndex: number): number {
    // Border + line height * lines. There is no padding.
    return 4 * 2 + (rowIndex + 1) * 18;
}

/** In px. */
function getRowWidth(rowIndex: number, columnsCount: number): number {
    const n = columnsCount;
    const k = rowIndex + 1;
    const nodes = factorial(n) / (factorial(k) * factorial(n - k));

    return nodes * (NODE_SIZE + NODE_X_GAP) - NODE_X_GAP;
}

const factorialCache = new Map<number, number>();

function factorial(x: number): number {
    if (x <= 1)
        return 1;

    const cached = factorialCache.get(x);
    if (cached !== undefined)
        return cached;

    const result = x * factorial(x - 1);
    factorialCache.set(x, result);

    return result;
}

const NodeComponent = memo(CustomNodeComponent);
const nodeTypes = {
    NodeComponent,
};

function CustomNodeComponent({ data }: NodeProps<RFNode>) {
    return (<>
        <div>
            {data.label}
        </div>
        <Handle
            id='source'
            type='source'
            position={Position.Top}
            isConnectable={false}
            className='opacity-0'
        />
        <Handle
            id='target'
            type='target'
            position={Position.Bottom}
            isConnectable={false}
            className='opacity-0'
        />
    </>);
}
