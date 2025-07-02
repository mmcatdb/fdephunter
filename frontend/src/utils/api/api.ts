import { type Empty } from '@/types/api/routes';
import { POST } from './routeFunctions';
import { assignment } from './routes/assignment';
import { dataset } from './routes/dataset';
import { view } from './routes/view';
import { workflow } from './routes/workflow';

function prepareAbort(): [ AbortSignal, () => void ] {
    const controller = new AbortController();

    return [
        controller.signal,
        () => {
            controller.abort();
        },
    ];
}

export const API = {
    assignment,
    dataset,
    view,
    workflow,
    prepareAbort,
    resetDatabase: POST<Empty, Empty>(
        () => `/demo/reset-database`,
    ),
};
