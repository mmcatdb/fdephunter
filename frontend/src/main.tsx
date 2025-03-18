import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import { HeroUIProvider } from '@heroui/react';
//import '@/assets/base.scss';
// import '@/assets/components.scss';
import '@/assets/index.css';
import { App } from './App.js';

ReactDOM.createRoot(document.getElementById('react-root')!).render(
    <StrictMode>
        <HeroUIProvider className='h-full'>
            <App />
        </HeroUIProvider>
    </StrictMode>,
);
