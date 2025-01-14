import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import { NextUIProvider } from '@nextui-org/react';
//import '@/assets/base.scss';
// import '@/assets/components.scss';
import '@/assets/index.css';
import App from './App.js';

ReactDOM.createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <NextUIProvider>
            <App />
        </NextUIProvider>
    </StrictMode>,
);
