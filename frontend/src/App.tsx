import { Button } from '@nextui-org/react';
import { Content, LeftBar, TopBar } from './components/layout';
import { RouterView, routes } from './router';
import { BrowserRouter, Link } from 'react-router-dom';

export default function App() {
    return (
        <BrowserRouter>
            <TopBar className='flex'>
                <Link to={routes.root}>
                    <Button className='fd-home-button'>
                        Home
                    </Button>
                </Link>
            </TopBar>
            <LeftBar className='pb-12' />
            <Content className='px-4'>
                <RouterView />
            </Content>
        </BrowserRouter>
    );
}
