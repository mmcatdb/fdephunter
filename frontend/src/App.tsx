import { Layout } from './components/layout';
import { RouterView } from './router';
import { BrowserRouter } from 'react-router-dom';

export function App() {
    return (
        <BrowserRouter future={future}>
            <Layout>
                <RouterView />
            </Layout>
        </BrowserRouter>
    );
}

const future = {
    v7_relativeSplatPath: true,
    v7_startTransition: true,
} as const;
