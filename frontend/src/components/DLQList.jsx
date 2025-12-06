import React from 'react';
import { useJobs } from '@/context/JobContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { formatDistanceToNow } from 'date-fns';
import { RefreshCcw } from 'lucide-react';
import toast from 'react-hot-toast';

const DLQList = () => {
    const { jobs, submitJob } = useJobs();

    // In a real app, this would fetch from a specific DLQ endpoint.
    // Here we simulate it by filtering jobs that are FAILED and have max retries.
    // Or just FAILED jobs.
    const dlqJobs = jobs.filter(job => job.status === 'FAILED');

    const handleRetry = async (job) => {
        try {
            // Re-submit the payload
            let payloadObj = {};
            try { payloadObj = JSON.parse(job.payload); } catch (e) { }

            await submitJob(payloadObj, null, job.maxRetries);
            toast.success(`Job ${job.id.substring(0, 8)}... requeued`);
        } catch (e) {
            // Error handling in context
        }
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Dead Letter Queue ({dlqJobs.length})</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="rounded-md border">
                    <table className="w-full text-sm text-left">
                        <thead className="bg-muted text-muted-foreground">
                            <tr>
                                <th className="p-4 font-medium">Job ID</th>
                                <th className="p-4 font-medium">Failed At</th>
                                <th className="p-4 font-medium">Reason</th>
                                <th className="p-4 font-medium text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {dlqJobs.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="p-4 text-center text-muted-foreground">No failed jobs found.</td>
                                </tr>
                            ) : (
                                dlqJobs.map(job => (
                                    <tr key={job.id} className="border-t hover:bg-muted/50 transition-colors">
                                        <td className="p-4 font-mono">{job.id}</td>
                                        <td className="p-4">
                                            {job.completedAt ? formatDistanceToNow(new Date(job.completedAt), { addSuffix: true }) : 'N/A'}
                                        </td>
                                        <td className="p-4 text-red-500 truncate max-w-xs" title="View details in Job card">
                                            Max retries exceeded
                                        </td>
                                        <td className="p-4 text-right">
                                            <Button size="sm" variant="outline" onClick={() => handleRetry(job)}>
                                                <RefreshCcw className="h-3 w-3 mr-1" />
                                                Retry
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </CardContent>
        </Card>
    );
};

export default DLQList;
