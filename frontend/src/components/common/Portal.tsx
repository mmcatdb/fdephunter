import { type ReactNode, useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

type PortalProps = {
    children?: ReactNode;
    to: string;
}

export default function Portal({ children, to }: PortalProps) {
    const [ target, setTarget ] = useState(document.getElementById(to));

    useEffect(() => {
        setTarget(document.getElementById(to));
    }, [ to ]);

    return target ? createPortal(children, target) : null;
}

export const portals = {
    topbar: 'top-bar-content',
};
