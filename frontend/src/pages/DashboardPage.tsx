import API from '@/utils/api';
import { useNavigate } from 'react-router';
import { routes } from '@/router';
import { Button, Col, Container, Row } from 'react-bootstrap';
import { Workflow } from '@/types/workflow';
import { Link } from 'react-router-dom';
import rawAPI from '@/utils/api/rawAPI';
import Portal, { portals } from '@/components/common/Portal';

export default function DashboardPage() {
    const navigate = useNavigate();

    async function continueToWorkflow() {
        const response = await API.workflows.create({}, {});
        if (!response.status)
            return;

        navigate(routes.workflow.detail.resolve({ workflowId: Workflow.fromServer(response.data).id }));
    }

    return (<>
        <Portal to={portals.topbar} >
            <TopbarToolbar />
        </Portal>
        <Container>
            <h1>A catching title!</h1>
            <p className='mt-5'>
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            </p>
            <p>
                Even more catching description. Wanna know more?
            </p>
            <Row className='mt-5'>
                <Col />
                <Col lg={4} md={6} xs={12}>
                    <Button
                        className='w-100'
                        onClick={continueToWorkflow}
                    >
                        Sure thing!
                    </Button>
                </Col>
                <Col />
            </Row>
        </Container>
    </>);
}

function TopbarToolbar() {
    function resetDatabase() {
        rawAPI.POST('/demo/initialize');
    }

    return (
        <div>
            <Link to={routes.root}>
                Home
            </Link>
            <Link to={routes.workflow.example} className='ms-3'>
                Workflow
            </Link>
            <Link to={routes.worker.example} className='ms-3'>
                Worker
            </Link>
            <Link to={routes.assignment.example} className='ms-3'>
                Assignment
            </Link>
            <Button onClick={resetDatabase} className='ms-3' variant='outline-primary'>
                Reset DB
            </Button>
        </div>
    );
}
