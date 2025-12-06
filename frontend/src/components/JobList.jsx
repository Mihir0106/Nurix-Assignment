import React, { useState, useMemo } from 'react';
import { useJobs } from '@/context/JobContext';
import JobCard from './JobCard';
import { Button } from '@/components/ui/button';
import { RefreshCw, Search } from 'lucide-react';

const CLASSES = {
    tab: "px-4 py-2 text-sm font-medium rounded-md transition-colors",
    activeTab: "bg-primary text-primary-foreground",
    inactiveTab: "text-muted-foreground hover:bg-muted"
};

const JobList = ({ limit }) => {
    const { jobs, refreshJobs, loading } = useJobs();
    const [filter, setFilter] = useState('ALL');
    const [search, setSearch] = useState('');

    const filteredJobs = useMemo(() => {
        let result = jobs;

        if (filter !== 'ALL') {
            result = result.filter(job => job.status === filter);
        }

        if (search) {
            const lowerSearch = search.toLowerCase();
            result = result.filter(job =>
                job.id.toLowerCase().includes(lowerSearch) ||
                (job.payload && job.payload.toLowerCase().includes(lowerSearch))
            );
        }

        // Apply limit if provided (e.g. for dashboard)
        if (limit) {
            result = result.slice(0, limit);
        }

        return result;
    }, [jobs, filter, search, limit]);

    return (
        <div className="space-y-4">
            <div className="flex flex-col sm:flex-row justify-between gap-4">
                {/* Tabs */}
                <div className="flex space-x-1 bg-muted p-1 rounded-md overflow-x-auto">
                    {['ALL', 'PENDING', 'RUNNING', 'COMPLETED', 'FAILED'].map(status => (
                        <button
                            key={status}
                            onClick={() => setFilter(status)}
                            className={`${CLASSES.tab} ${filter === status ? CLASSES.activeTab : CLASSES.inactiveTab}`}
                        >
                            {status === 'ALL' ? 'All Jobs' : status}
                        </button>
                    ))}
                </div>

                <div className="flex gap-2">
                    <div className="relative">
                        <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                        <input
                            type="text"
                            placeholder="Search ID or Payload..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="pl-8 h-10 w-full sm:w-64 rounded-md border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                        />
                    </div>
                    <Button variant="outline" size="icon" onClick={refreshJobs} disabled={loading}>
                        <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                    </Button>
                </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {filteredJobs.length > 0 ? (
                    filteredJobs.map(job => (
                        <JobCard key={job.id} job={job} />
                    ))
                ) : (
                    <div className="col-span-full text-center py-10 text-muted-foreground">
                        No jobs found matching your criteria.
                    </div>
                )}
            </div>

            {/* Simple footer pagination placeholder if needed */}
            {!limit && filteredJobs.length >= 20 && (
                <div className="flex justify-center pt-4">
                    <Button variant="ghost" disabled>Load More (Pagination not implemented in demo)</Button>
                </div>
            )}
        </div>
    );
};

export default JobList;
