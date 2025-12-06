import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, List, AlertOctagon, Terminal, Sun, Moon } from 'lucide-react';
import { Toaster } from 'react-hot-toast';
import { useJobs } from '@/context/JobContext';
import { Button } from './ui/button';

const SidebarItem = ({ to, icon: Icon, label, active }) => (
    <Link to={to} className={`flex items-center gap-3 px-3 py-2 rounded-md transition-colors ${active ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'}`}>
        <Icon className="h-4 w-4" />
        <span className="text-sm font-medium">{label}</span>
    </Link>
);

const Layout = ({ children }) => {
    const location = useLocation();
    const { tenantId } = useJobs();

    // Quick dark mode toggle implementation
    const toggleDarkMode = () => {
        document.documentElement.classList.toggle('dark');
    };

    return (
        <div className="min-h-screen bg-background flex">
            {/* Sidebar */}
            <aside className="w-64 border-r bg-card hidden md:flex flex-col">
                <div className="p-6">
                    <div className="flex items-center gap-2 font-bold text-xl">
                        <Terminal className="h-6 w-6 text-primary" />
                        <span>TaskQueue</span>
                    </div>
                    <div className="text-xs text-muted-foreground mt-1 px-1">
                        Tenant: {tenantId}
                    </div>
                </div>

                <nav className="flex-1 px-4 space-y-1">
                    <SidebarItem to="/" icon={LayoutDashboard} label="Dashboard" active={location.pathname === '/'} />
                    <SidebarItem to="/jobs" icon={List} label="Jobs" active={location.pathname === '/jobs'} />
                    <SidebarItem to="/dlq" icon={AlertOctagon} label="Dead Letter Queue" active={location.pathname === '/dlq'} />
                </nav>

                <div className="p-4 border-t">
                    <Button variant="ghost" size="sm" className="w-full justify-start" onClick={toggleDarkMode}>
                        <Sun className="h-4 w-4 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
                        <Moon className="absolute h-4 w-4 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
                        <span className="ml-2">Toggle Theme</span>
                    </Button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col min-w-0">
                <header className="h-16 border-b flex items-center justify-between px-6 bg-card/50 backdrop-blur">
                    <h1 className="text-lg font-semibold">
                        {location.pathname === '/' ? 'Dashboard' :
                            location.pathname === '/jobs' ? 'Job Management' :
                                location.pathname === '/dlq' ? 'Dead Letter Queue' : 'Page'}
                    </h1>
                    {/* Mobile menu trigger could go here */}
                </header>

                <div className="flex-1 p-6 overflow-auto">
                    {children}
                </div>
            </main>

            <Toaster position="bottom-right" />
        </div>
    );
};

export default Layout;
