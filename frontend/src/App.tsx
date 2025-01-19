import { router } from './router';
import { RouterProvider } from 'react-router-dom';

export function App() {
    return (
        <RouterProvider router={router} future={future}/>
    );
}

const future = {
    v7_relativeSplatPath: true,
    v7_startTransition: true,
} as const;
