import React from 'react';
import JobList from '@/components/JobList';
import JobSubmitForm from '@/components/JobSubmitForm';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';

const JobsPage = () => {
    const [showForm, setShowForm] = React.useState(false);

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold tracking-tight">Job Management</h2>
                <Button onClick={() => setShowForm(!showForm)}>
                    <Plus className="h-4 w-4 mr-2" />
                    New Job
                </Button>
            </div>

            {showForm && (
                <div className="mb-6">
                    <JobSubmitForm />
                </div>
            )}

            <JobList />
        </div>
    );
};

export default JobsPage;
