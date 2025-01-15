import clsx from 'clsx';
import { useState, type ReactNode, type SetStateAction, type Dispatch, createContext, useContext, useMemo, useEffect } from 'react';
import { TbHome, TbLayoutSidebarLeftCollapse, TbLayoutSidebarRightCollapse } from 'react-icons/tb';
import { Button, ScrollShadow } from '@nextui-org/react';
import { Link } from 'react-router-dom';
import { routes } from '@/router';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { BiCog } from 'react-icons/bi';
import { Portal } from './common/Portal';

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

type LayoutProps = {
    children?: ReactNode;
};

export function Layout({ children }: LayoutProps) {
    const [ state, setState ] = useState(defaultLayoutState);
    const context = useMemo(() => ({ state, setState }), [ state, setState ]);

    useEffect(() => {
        const themeListener = (event: MediaQueryListEvent) => {
            setState(prev => ({ ...prev, isOSThemeDark: event.matches }));
        };

        windowThemeMatcher.addEventListener('change', themeListener);

        return () => {
            windowThemeMatcher.removeEventListener('change', themeListener);
        };
    }, []);

    const isDark = state.isLocalThemeDark ?? state.isOSThemeDark;

    return (
        <layoutContext.Provider value={context}>
            <div
                className={clsx(`
                    min-w-[600px] min-h-full pt-14 flex flex-col transition-margin !ease-in-out
                    text-foreground-800 bg-content1 font-medium
                    `,
                isDark && 'fd-global-dark dark',
                state.isCollapsed ? 'ms-0' : 'ms-80',
                )}
            >
                <TopBar />

                <Sidebar />

                <Content>
                    {children}
                </Content>
            </div>
        </layoutContext.Provider>
    );
}

function TopBar() {
    const { state: { isCollapsed, isLocalThemeDark }, setState } = useLayout();

    return (
        <nav className={clsx('fixed top-0 right-0 left-0 z-10')}>
            <ScrollShadow orientation='horizontal' hideScrollBar>
                <div className='min-w-[600px] w-full h-14 flex justify-center bg-primary-100'>

                    <div className='max-w-xs px-2 flex items-center gap-2'>
                        <Button isIconOnly size='sm' variant='light' onPress={() => setState(prev => ({ ...prev, isCollapsed: !prev.isCollapsed }))}>
                            {isCollapsed ? (
                                <TbLayoutSidebarRightCollapse size={24} />
                            ) : (
                                <TbLayoutSidebarLeftCollapse size={24} />
                            )}
                        </Button>

                        <Button isIconOnly size='sm' variant='light' onPress={() => setState(prev => ({ ...prev, isLocalThemeDark: prev.isLocalThemeDark === undefined ? false : prev.isLocalThemeDark ? undefined : true }))}>
                            {isLocalThemeDark === undefined ? (
                                <BiCog size={24} />
                            ) : isLocalThemeDark ? (
                                <MdOutlineDarkMode size={24} />
                            ) : (
                                <MdOutlineLightMode size={24} />
                            )}
                        </Button>

                        <Button isIconOnly size='sm' variant='light' as={Link} to={routes.root}>
                            <TbHome size={24} />
                        </Button>
                    </div>

                    <div id={Portal.targets.topbar} className='grow flex items-center justify-center' />

                    <div className='max-w-xs' />

                </div>
            </ScrollShadow>
        </nav>
    );
}

type SidebarProps = {
    children?: ReactNode;
};

function Sidebar({ children }: SidebarProps) {
    const { state: { isCollapsed } } = useLayout();

    return (
        <aside
            id={Portal.targets.sidebar}
            className={clsx(
                'fixed top-14 bottom-0 left-0 overflow-hidden transition-width !ease-in-out bg-content2',
                isCollapsed ? 'w-0' : 'w-80',
            )}
        >
            {children}
        </aside>
    );
}

type ContentProps = {
    children?: ReactNode;
};

function Content({ children }: ContentProps) {
    return (
        // Id for uniqueness and accessibility.
        <main id='main-content' className='grow flex'>
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
        <div className={clsx('w-full max-w-7xl mx-auto px-4 py-8', className)}>
            {children}
        </div>
    );
}
