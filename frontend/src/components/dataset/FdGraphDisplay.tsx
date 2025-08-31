import { memo, type ReactNode, useMemo, useState } from 'react';
import { NODE_OPTIONS, type RFGraph, type RfNode, type RfEdge } from '@/types/functionalDependency';
import { Handle, type NodeProps, Position, ReactFlow } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { computeColumnIndexesForLatticeRow, computeEdgesForLatticeCell, type Lattice, CellType, nChooseK } from '@/types/examples';
import { Button, cn, Divider, Popover, PopoverContent, PopoverTrigger, Select, SelectItem, type SharedSelection, Switch } from '@heroui/react';
import clsx from 'clsx';

export function LatticeDisplay({ lattices }: { lattices: Lattice[] }) {
    const [ classIndex, setClassIndex ] = useState(new Set([ '0' ]));
    const classItems = useMemo(() => lattices.map((lattice, index) => ({ value: index, label: lattice.classColumn })), [ lattices ]);
    const lattice = lattices[Number(classIndex.values().next().value!)];

    const [ showAllRows, setShowAllRows ] = useState(true);
    const [ rowIndex, setRowIndex ] = useState(0);

    const selectedIndex = showAllRows ? undefined : rowIndex;
    const rfGraph = useMemo(() => createLatticeGraph(lattice, selectedIndex), [ lattice, selectedIndex ]);

    return (
        <div className='w-full h-full flex flex-col'>
            <div className={clsx('h-10 flex items-center justify-center gap-8')}>
                <Popover placement='bottom-start'>
                    <PopoverTrigger>
                        <Button>Legend</Button>
                    </PopoverTrigger>
                    <PopoverContent>
                        <Legend />
                    </PopoverContent>
                </Popover>

                <Select
                    label='Class'
                    labelPlacement='outside-left'
                    items={classItems}
                    selectedKeys={classIndex}
                    disallowEmptySelection
                    onSelectionChange={setClassIndex as (keys: SharedSelection) => void}
                    className='max-w-sm'
                >
                    {item => (
                        <SelectItem key={item.value}>{item.label}</SelectItem>
                    )}
                </Select>

                <Switch isSelected={showAllRows} onValueChange={setShowAllRows}>
                    Show all rows?
                </Switch>

                {!showAllRows && (<>
                    <Divider orientation='vertical' />

                    <div>
                        Row:{' '}
                        <span className='tabular-nums text-lg font-bold'>
                            {rowIndex + 1}
                        </span>
                    </div>

                    <div className='space-x-2'>
                        <Button onPress={() => setRowIndex(prev => Math.max(0, prev - 1))} isDisabled={showAllRows || rowIndex === 0}>
                            Previous
                        </Button>

                        <Button onPress={() => setRowIndex(prev => Math.min(lattice.rows.length - 1, prev + 1))} isDisabled={showAllRows || rowIndex === lattice.rows.length - 1}>
                            Next
                        </Button>
                    </div>
                </>)}
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

function Legend() {
    return (
        <div className='grid grid-cols-[repeat(3,auto)] gap-x-8 gap-y-2'>
            <LegendItem type={CellType.GenuineFinal} text='Genuine FD' />
            <LegendItem type={CellType.InvalidFinal} text={<>Final <McCharacter /> element (initial)</>} />
            <LegendItem type={CellType.FakeFinal} text={<>Final <McCharacter /> element (eliminated FD)</>} />

            <LegendItem type={CellType.GenuineTemp} text='Candidate for genuine FD' />
            <LegendItem type={CellType.InvalidTemp} text={<>Initial <McCharacter /> element</>} />
            <LegendItem type={CellType.FakeTemp} text='Targeted FD' />

            <LegendItem type={CellType.GenuineDerived} text='Derived FD' />
            <LegendItem type={CellType.InvalidDerived} text={<>Subset of <McCharacter /> element</>} />
            <LegendItem type={CellType.FakeDerived} text={<>Subset of <McCharacter /> element (eliminated FD)</>} />
        </div>
    );
}

function McCharacter() {
    return (
        <span className='italic'>M<sub>C</sub></span>
    );
}

function LegendItem({ type, text }: { type: CellType, text: ReactNode }) {
    return (
        <div className='flex items-center gap-2'>
            <div className={clsx('w-10 flex items-center justify-center border-2 text-black', getCellTypeClassName(type))}>
                abc
            </div>
            <div>{text}</div>
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

    const nodes: RfNode[] = [];
    const edges: RfEdge[] = [];

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

function createRowNodes(lattice: Lattice, rowIndex: number, columnIndexes: number[][], xOffset: number, yOffset: number): RfNode[] {
    const types = lattice.rows[rowIndex];

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
                getCellTypeClassName(type),
            ),
        } satisfies RfNode;
    });
}

function computeNodeId(cell: number[]): string {
    return cell.join('-');
}

function createRowEdges(lattice: Lattice, columnIndexes: number[][]): RfEdge[] {
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

const bold = [ CellType.InvalidFinal, CellType.GenuineFinal, CellType.FakeFinal ];
const white = [ CellType.InvalidDerived, CellType.GenuineDerived, CellType.FakeDerived ];

const red = [ CellType.InvalidFinal, CellType.InvalidTemp, CellType.InvalidDerived ];
const blue = [ CellType.GenuineFinal, CellType.GenuineTemp, CellType.GenuineDerived ];
const yellow = [ CellType.FakeFinal, CellType.FakeTemp, CellType.FakeDerived ];

function getCellTypeClassName(type: CellType): string {
    return cn(
        bold.includes(type) && 'font-bold',
        red.includes(type) && '!border-red-600 !bg-red-300',
        blue.includes(type) && '!border-blue-600 !bg-blue-300',
        yellow.includes(type) && '!border-yellow-600 !bg-yellow-200',
        white.includes(type) && '!bg-white',
    );
}

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
function getRowWidth(rowIndex: number, columnCount: number): number {
    const n = columnCount;
    const k = rowIndex + 1;
    const nodes = nChooseK(n, k);

    return nodes * (NODE_SIZE + NODE_X_GAP) - NODE_X_GAP;
}


const NodeComponent = memo(CustomNodeComponent);
const nodeTypes = {
    NodeComponent,
};

function CustomNodeComponent({ data }: NodeProps<RfNode>) {
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
