import { routes } from '@/router';
import { Workflow } from '@/types/workflow';
import { useNavigate } from 'react-router';
import { Button } from '@heroui/react';
import { Page } from '@/components/layout';
import { useState } from 'react';
import { API } from '@/utils/api/api';

const PAPER_HREF = 'https://vldb.org/pvldb/volumes/18/paper/FDepHunter%3A%20Harnessing%20Negative%20Examples%20to%20Expose%20Fakes%20and%20Reveal%20Ghosts';

export function LandingPage() {
    const [ isFetching, setIsFetching ] = useState(false);
    const navigate = useNavigate();

    async function continueToWorkflow() {
        setIsFetching(true);
        const response = await API.workflow.createWorkflow({});
        if (!response.status) {
            setIsFetching(false);
            return;
        }

        void navigate(routes.workflow.settings.resolve({ workflowId: Workflow.fromResponse(response.data).id }));
    }

    return (
        <Page>
            <h1 className='text-lg'>FDepHunter</h1>

            <p className='mt-12'>
                Functional dependencies (FDs) are key to understanding your data, but standard discovery methods can miss important insights. FDepHunter goes beyond traditional approaches by eliminating false positives (coincidental FDs) and revealing hidden patterns (ghost FDs) that standard techniques overlook.
                <br /><br />

                By enhancing datasets with intelligently generated examples, FDepHunter improves accuracy and provides a clearer picture of real-world data relationships. Whether {'you\'re'} profiling data for research, optimization, or compliance, FDepHunter helps you make more informed decisions.
                <br /><br />

                Try FDepHunter today and uncover the true structure of your data!
            </p>

            <div className='mt-12 flex flex-col items-center gap-4'>
                <Button color='primary' className='md:w-1/2 lg:w-1/3' onPress={continueToWorkflow} isLoading={isFetching}>
                    Sure thing!
                </Button>

                <a href={PAPER_HREF} className='md:w-1/2 lg:w-1/3' target='_blank' rel='noreferrer'>
                    <Button color='secondary' className='w-full'>
                        Read the paper
                    </Button>
                </a>
            </div>
        </Page>
    );
}
