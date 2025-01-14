import clsx from 'clsx';
import { useState, type ReactNode, useEffect } from 'react';
import { TbLayoutSidebarLeftCollapse, TbLayoutSidebarRightCollapse } from 'react-icons/tb';
import { portals } from './common/Portal';

type LayoutElementProps = {
    children?: ReactNode;
    className?: string;
};

export function TopBar({ children, className }: LayoutElementProps) {
    return (
        <header id='top-bar' className={className}>
            <div id='top-bar-left'>
                {children}
            </div>
            <div id={portals.topbar} className='grow flex items-center justify-center'/>
            <div id='top-bar-right' />
        </header>
    );
}

const BREAKPOINT_COLLAPSE = 924;

export function LeftBar({ children, className }: LayoutElementProps) {
    const [ isCollapsed, setIsCollapsed ] = useState(false);

    useEffect(() => {
        setIsCollapsed(window.innerWidth < BREAKPOINT_COLLAPSE);
    }, []);

    return (<>
        {/* TODO Replace by button. */}
        <div className='fd-collapse-button' onClick={() => setIsCollapsed(!isCollapsed)}>
            <CollapseButton isCollapsed={isCollapsed} />
        </div>
        <nav id='left-bar' className={clsx(className, isCollapsed && 'collapsed')}>
            {children}
        </nav>
    </>);
}

type CollapseButtonProps = {
    isCollapsed: boolean;
};

function CollapseButton({ isCollapsed }: CollapseButtonProps) {
    return isCollapsed ? (
        <TbLayoutSidebarRightCollapse size={32} />
    ) : (
        <TbLayoutSidebarLeftCollapse size={32} />
    );
}

export function Content({ children, className }: LayoutElementProps) {
    return (
        <main id='content'>
            <div id='content-inner' className={className}>
                {children}
            </div>
        </main>
    );
}
