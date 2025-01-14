import type { FDEdge, FDGraph } from '@/types/FD';
import { Badge, Col, Row } from 'react-bootstrap';
import { FaArrowRight } from 'react-icons/fa';

type FDListProps = {
    graph: FDGraph;
};

export default function FDList({ graph }: FDListProps) {
    return (
        <div>
            {graph.edges.map(edge => (
                <FDRow key={edge.id} edge={edge} />
            ))}
        </div>
    );
}

type FDRowProps = {
    edge: FDEdge;
};

function FDRow({ edge }: FDRowProps) {
    return (
        <Row className='py-1'>
            <Col xs={1}>{edge.id}</Col>
            <Col xs={6}>{edge.source.columns.map(name => <ColumnNameBadge key={name} name={name} />)}</Col>
            <Col xs={2}><FaArrowRight size={20} /></Col>
            <Col xs={3}>{edge.target.columns.map(name => <ColumnNameBadge key={name} name={name} />)}</Col>
        </Row>
    );
}

type ColumnNameBadgeProps = {
    name: string;
}

function ColumnNameBadge({ name }: ColumnNameBadgeProps) {
    return (
        <Badge className='fd-column-name-badge'>{name}</Badge>
    );
}