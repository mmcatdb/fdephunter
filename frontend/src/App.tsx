import { Button } from 'react-bootstrap';
import { Content, LeftBar, TopBar } from './components/layout';
import { RouterView, routes } from './router';
import { BrowserRouter, Link } from 'react-router-dom';

export default function App() {
    return (
        <BrowserRouter>
            <TopBar className='d-flex'>
                <Link to={routes.root}>
                    <Button className='fd-home-button'>
                        Home
                    </Button>
                </Link>
            </TopBar>
            <LeftBar className='pb-5' />
            <Content className='px-3'>
                <RouterView />
            </Content>
        </BrowserRouter>
    );
}
