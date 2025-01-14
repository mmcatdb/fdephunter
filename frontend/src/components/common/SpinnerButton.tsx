import React, { useState, useRef, useEffect, useMemo } from 'react';
import { Button, type ButtonProps, Spinner } from 'react-bootstrap';

type SpinnerButtonProps = Omit<ButtonProps, 'fetching'> & {
    fetching: boolean | undefined;
};

/**
 * This component acts like a button that turns into a spinner whenewer fetching === true.
 * The button is disabled, however its dimensions remain constant.
 */
export default function SpinnerButton({ fetching, disabled, ...rest }: SpinnerButtonProps) {
    const [ maxWidth, setMaxWidth ] = useState<number>();
    const [ maxHeight, setMaxHeight ] = useState<number>();
    const contentRef = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        if (!contentRef.current)
            return;
        
        const newWidth = contentRef.current.getBoundingClientRect().width;
        setMaxWidth((current) => (!current || newWidth > current) ? newWidth : current);

        const newHeight = contentRef.current.getBoundingClientRect().height;
        setMaxHeight((current) => (!current || newHeight > current) ? newHeight : current);
    }, [ fetching ]);

    const variant = useMemo(() => {
        if (rest.variant?.includes('outline'))
            return 'dark';
        else
            return 'light';
    }, [ rest.variant ]);

    return (
        <Button
            variant='primary'
            {...rest}
            disabled={!!fetching || disabled}
            ref={contentRef}
            style={fetching ? { width: maxWidth, height: maxHeight } : {}}
        >
            {fetching ?
                <Spinner
                    size='sm'
                    variant={variant}
                    animation='border'
                /> :
                rest.children
            }
        </Button>
    );
}
