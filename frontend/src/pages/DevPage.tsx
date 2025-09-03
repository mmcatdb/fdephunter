import { Button } from '@heroui/react';
import { Page } from '@/components/layout';
import { useState } from 'react';
import { API } from '@/utils/api/api';

export function DevPage() {
    const [ isFetching, setIsFetching ] = useState(false);

    function resetDatabase() {
        setIsFetching(true);
        void API.resetDatabase({});
        setIsFetching(false);
    }

    return (
        <Page>
            <h1 className='text-lg'>{'You\'re a developer, Harry!'}</h1>

            <div className='mt-4'>
                <Button color='danger' onPress={resetDatabase} isLoading={isFetching}>
                    Reset DB
                </Button>
            </div>
        </Page>
    );
}
