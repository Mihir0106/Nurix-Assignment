import React from 'react';
import DLQList from '@/components/DLQList';

const DLQPage = () => {
    return (
        <div className="space-y-6">
            <h2 className="text-2xl font-bold tracking-tight">Dead Letter Queue</h2>
            <p className="text-muted-foreground">Manage failed jobs and retry them if necessary.</p>
            <DLQList />
        </div>
    );
};

export default DLQPage;
