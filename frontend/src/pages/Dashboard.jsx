import React from 'react';
import StatsPanel from '@/components/StatsPanel';
import JobSubmitForm from '@/components/JobSubmitForm';
import JobList from '@/components/JobList';

const Dashboard = () => {
    return (
        <div className="space-y-6">
            <StatsPanel />

            <div className="grid md:grid-cols-3 gap-6">
                <div className="md:col-span-1">
                    <JobSubmitForm />
                </div>
                <div className="md:col-span-2 space-y-4">
                    <h2 className="text-lg font-semibold">Recent Jobs</h2>
                    <JobList limit={10} />
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
