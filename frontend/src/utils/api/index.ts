import { approaches } from './routes/approaches';
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
    // approaches,
    // assignments,
    // datasets,
    // workflows,
    prepareAbort,
};
