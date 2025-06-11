import { assignments } from './routes/assignments';
import { datasets } from './routes/datasets';
import { workflows } from './routes/workflows';

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
    // assignments,
    // datasets,
    // workflows,
    prepareAbort,
};
