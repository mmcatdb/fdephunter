import clsx from 'clsx';
import { useState, type ReactNode, type SetStateAction, type Dispatch, createContext, useContext, useMemo, useEffect } from 'react';
import { TbHome, TbLayoutSidebarLeftCollapse, TbLayoutSidebarRightCollapse } from 'react-icons/tb';
import { Button, cn, ScrollShadow } from '@nextui-org/react';
import { Link, Outlet } from 'react-router';
import { routes } from '@/router';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { Portal } from './common/Portal';
import { FaGithub } from 'react-icons/fa';
import { FaDisplay } from 'react-icons/fa6';

type LayoutState = {
    /** Whether the sidebar is collapsed. */
    isCollapsed: boolean;
    /** Whether the user prefers the dark theme in the OS. */
    isOSThemeDark: boolean;
    /** Undefined means the OS theme should be used instead. */
    isLocalThemeDark: boolean | undefined;
};

const windowThemeMatcher = window.matchMedia('(prefers-color-scheme: dark)');

const defaultLayoutState: LayoutState = {
    isCollapsed: window.innerWidth < 924,
    isOSThemeDark: windowThemeMatcher.matches,
    // TODO use local storage
    isLocalThemeDark: undefined,
};

type LayoutContext = {
    state: LayoutState;
    setState: Dispatch<SetStateAction<LayoutState>>;
};

const layoutContext = createContext<LayoutContext | undefined>(undefined);

function useLayout(): LayoutContext {
    const context = useContext(layoutContext);
    if (context === undefined)
        throw new Error('Layout context must be used within a LayoutProvider');

    return context;
}

export function Layout() {
    const [ state, setState ] = useState(defaultLayoutState);
    const context = useMemo(() => ({ state, setState }), [ state, setState ]);

    useEffect(() => {
        const c = new AbortController();

        windowThemeMatcher.addEventListener('change', (event: MediaQueryListEvent) => {
            setState(prev => ({ ...prev, isOSThemeDark: event.matches }));
        }, { signal: c.signal });

        return () => c.abort();
    }, []);

    const isDark = state.isLocalThemeDark ?? state.isOSThemeDark;

    useEffect(() => {
        document.documentElement.classList.toggle('dark', isDark);
        document.documentElement.classList.toggle('light', !isDark);
    }, [ isDark ]);

    return (
        <layoutContext.Provider value={context}>
            <div className='group/layout min-w-fit min-h-full pt-14 flex text-foreground-800 bg-content1 font-medium'>
                <Topbar />

                <div id='sidebar-portal' />

                <Content>
                    <Outlet />
                </Content>
            </div>
        </layoutContext.Provider>
    );
}

function Topbar() {
    const { state: { isCollapsed, isLocalThemeDark }, setState } = useLayout();

    return (
        <nav className={clsx('fixed top-0 right-0 left-0 z-10')}>
            <ScrollShadow orientation='horizontal' hideScrollBar>
                <div className='min-w-[600px] w-full h-14 flex justify-center bg-content1'>

                    <div className='w-40 px-2 flex items-center gap-2'>
                        <Button
                            isIconOnly
                            size='sm'
                            variant='light'
                            color='primary'
                            onPress={() => setState(prev => ({ ...prev, isCollapsed: !prev.isCollapsed }))}
                            className='group-has-[#sidebar-portal:empty]/layout:hidden'
                        >
                            {isCollapsed ? (
                                <TbLayoutSidebarRightCollapse size={24} />
                            ) : (
                                <TbLayoutSidebarLeftCollapse size={24} />
                            )}
                        </Button>

                        <Button
                            isIconOnly
                            size='sm'
                            variant='light'
                            color='primary'
                            onPress={() => setState(prev => ({ ...prev, isLocalThemeDark: prev.isLocalThemeDark === undefined ? false : prev.isLocalThemeDark ? undefined : true }))}
                        >
                            {isLocalThemeDark === undefined ? (
                                <FaDisplay size={20} />
                            ) : isLocalThemeDark ? (
                                <MdOutlineDarkMode size={24} />
                            ) : (
                                <MdOutlineLightMode size={24} />
                            )}
                        </Button>

                        <Button isIconOnly size='sm' variant='light' color='primary' as={Link} to={routes.root}>
                            <TbHome size={24} />
                        </Button>
                    </div>

                    <div id='topbar-sidebar-spacer-portal' />

                    <div id='topbar-content-portal' className='grow flex items-center justify-center' />

                    <div className='w-40 px-2 flex items-center justify-end gap-2'>
                        <Button
                            isIconOnly
                            size='sm'
                            variant='light'
                            color='primary'
                            as={'a'}
                            href='https://github.com/mmcatdb/fdephunter'
                            target='_blank'
                            rel='noreferrer'
                        >
                            <FaGithub size={24} />
                        </Button>
                    </div>

                </div>
            </ScrollShadow>
        </nav>
    );
}

type TopbarContentProps = {
    children?: ReactNode;
};

export function TopbarContent({ children }: TopbarContentProps) {
    return (
        <Portal to='topbar-content-portal'>
            {children}
        </Portal>
    );
}

type SidebarProps = {
    children?: ReactNode;
};

export function Sidebar({ children }: SidebarProps) {
    const { state: { isCollapsed } } = useLayout();

    return (<>
        <Portal to='sidebar-portal'>
            <aside className={clsx('fixed top-14 left-0 z-20 h-[calc(100vh_-_56px)] overflow-hidden transition-width !ease-in-out bg-content2 lg:bg-content1', isCollapsed ? 'w-0' : 'w-80')}>
                {children}
            </aside>

            {/* Placeholder so that the fixed sidebar takes some space. */}
            <div
                className={clsx(
                    'shrink-0 max-lg:hidden left-0 transition-width !ease-in-out',
                    isCollapsed ? 'w-0' : 'w-80',
                )}
            />
        </Portal>

        {/* This keeps the topbar content aligned with the page content. */}
        <Portal to='topbar-sidebar-spacer-portal'>
            <div className={clsx('shrink-0 max-lg:hidden transition-width !ease-in-out', isCollapsed ? 'w-0' : 'w-80')} />
        </Portal>
    </>);
}

type ContentProps = {
    children?: ReactNode;
};

function Content({ children }: ContentProps) {
    return (
        // Id for uniqueness and accessibility.
        <main id='main-content' className='grow min-w-[600px] flex'>
            {children}
        </main>
    );
}

type PageProps = {
    children?: ReactNode;
    className?: string;
};

export function Page({ children, className }: PageProps) {
    return (
        <div className={cn('w-full max-w-7xl mx-auto px-4 py-8', className)}>
            {children}
        </div>
    );
}
