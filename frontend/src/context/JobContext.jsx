import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { jobService } from '../services/api';
import { webSocketService } from '../services/websocket';
import toast from 'react-hot-toast';

const JobContext = createContext();

export const JobProvider = ({ children, tenantId = 'tenant-1' }) => {
    const [jobs, setJobs] = useState([]);
    const [stats, setStats] = useState({
        total: 0,
        completed: 0,
        running: 0,
        failed: 0,
        pending: 0
    });
    const [loading, setLoading] = useState(true);

    const refreshJobs = useCallback(async () => {
        setLoading(true);
        try {
            const response = await jobService.listJobs(tenantId, 0, 100); // Fetch top 100 for now
            setJobs(response.data.content);
        } catch (error) {
            console.error('Failed to fetch jobs', error);
            toast.error('Failed to load jobs');
        } finally {
            setLoading(false);
        }
    }, [tenantId]);

    const handleJobUpdate = useCallback((updatedJob) => {
        setJobs(prevJobs => {
            const index = prevJobs.findIndex(j => j.id === updatedJob.id);
            if (index > -1) {
                const newJobs = [...prevJobs];
                newJobs[index] = updatedJob;
                return newJobs;
            } else {
                return [updatedJob, ...prevJobs];
            }
        });

        // Simple visible toast for important status changes
        if (updatedJob.status === 'COMPLETED') {
            toast.success(`Job ${updatedJob.id.substring(0, 8)}... Completed!`);
        } else if (updatedJob.status === 'FAILED') {
            toast.error(`Job ${updatedJob.id.substring(0, 8)}... Failed!`);
        }
    }, []);

    useEffect(() => {
        refreshJobs();

        webSocketService.connect(() => {
            webSocketService.subscribe(`/topic/jobs/${tenantId}`, handleJobUpdate);
            // Also subscribe to app init if needed, but we used REST for init
        });

        return () => {
            webSocketService.disconnect();
        };
    }, [tenantId, refreshJobs, handleJobUpdate]);

    // Calculate stats from jobs (client-side approximation)
    useEffect(() => {
        const newStats = jobs.reduce((acc, job) => {
            acc.total++;
            acc[job.status.toLowerCase()] = (acc[job.status.toLowerCase()] || 0) + 1;
            return acc;
        }, { total: 0, completed: 0, running: 0, failed: 0, pending: 0 });
        setStats(newStats);
    }, [jobs]);

    const submitJob = async (payload, idempotencyKey, maxRetries) => {
        try {
            await jobService.submitJob(payload, tenantId, idempotencyKey, maxRetries);
            toast.success('Job submitted');
        } catch (error) {
            toast.error('Failed to submit job');
            throw error;
        }
    };

    const cancelJob = async (jobId) => {
        try {
            await jobService.cancelJob(jobId, tenantId);
            toast.success('Job cancellation requested');
        } catch (error) {
            toast.error('Failed to cancel job');
        }
    };

    return (
        <JobContext.Provider value={{
            jobs,
            stats,
            loading,
            refreshJobs,
            submitJob,
            cancelJob,
            tenantId
        }}>
            {children}
        </JobContext.Provider>
    );
};

export const useJobs = () => useContext(JobContext);
