import React, { useState } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { formatDistanceToNow } from 'date-fns';
import { ChevronDown, ChevronUp, Clock, RefreshCw, XCircle, CheckCircle, AlertTriangle, FileJson, Copy } from 'lucide-react';
import { useJobs } from '@/context/JobContext';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';

const StatusIcon = ({ status }) => {
    switch (status) {
        case 'PENDING': return <Clock className="h-4 w-4" />;
        case 'RUNNING': return <RefreshCw className="h-4 w-4 animate-spin" />;
        case 'COMPLETED': return <CheckCircle className="h-4 w-4" />;
        case 'FAILED': return <AlertTriangle className="h-4 w-4" />;
        case 'CANCELLED': return <XCircle className="h-4 w-4" />;
        default: return null;
    }
};

const JobCard = ({ job }) => {
    const [expanded, setExpanded] = useState(false);
    const { cancelJob } = useJobs();

    const handleCopy = (text) => {
        navigator.clipboard.writeText(text);
        toast.success("Copied to clipboard");
    };

    const handleCancel = (e) => {
        e.stopPropagation();
        if (confirm('Are you sure you want to cancel this job?')) {
            cancelJob(job.id);
        }
    };

    return (
        <Card className="hover:shadow-md transition-shadow cursor-pointer overflow-hidden border-l-4"
            style={{ borderLeftColor: getStatusColor(job.status) }}
            onClick={() => setExpanded(!expanded)}>
            <CardHeader className="p-4 pb-2">
                <div className="flex justify-between items-start">
                    <div className="flex flex-col gap-1">
                        <div className="flex items-center gap-2">
                            <span className="font-mono text-xs text-muted-foreground">ID: {job.id.substring(0, 8)}...</span>
                            <Badge variant={job.status.toLowerCase()} className="gap-1 pl-1">
                                <StatusIcon status={job.status} />
                                {job.status}
                            </Badge>
                        </div>
                        <div className="text-xs text-muted-foreground flex items-center gap-1">
                            Create: {formatDistanceToNow(new Date(job.createdAt), { addSuffix: true })}
                        </div>
                    </div>
                    <div>
                        {/* Action Buttons could go here */}
                        {expanded ? <ChevronUp className="h-4 w-4 text-muted-foreground" /> : <ChevronDown className="h-4 w-4 text-muted-foreground" />}
                    </div>
                </div>
            </CardHeader>
            <CardContent className="p-4 pt-2">
                {/* Duration calculation if needed */}
                {job.startedAt && (
                    <div className="text-xs text-muted-foreground mt-1">
                        Start: {formatDistanceToNow(new Date(job.startedAt), { addSuffix: true })}
                    </div>
                )}
            </CardContent>

            <AnimatePresence>
                {expanded && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.2 }}
                        className="border-t bg-muted/20"
                    >
                        <div className="p-4 space-y-4">
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div>
                                    <p className="font-semibold text-muted-foreground">Trace ID</p>
                                    <div className="flex items-center gap-2 font-mono text-xs">
                                        {job.traceId || 'N/A'}
                                        {job.traceId && (
                                            <Button variant="ghost" size="icon" className="h-4 w-4" onClick={(e) => { e.stopPropagation(); handleCopy(job.traceId); }}>
                                                <Copy className="h-3 w-3" />
                                            </Button>
                                        )}
                                    </div>
                                </div>
                                <div>
                                    <p className="font-semibold text-muted-foreground">Retries</p>
                                    <p>{job.retryCount || 0} / {job.maxRetries}</p>
                                </div>
                            </div>

                            <div>
                                <p className="font-semibold text-muted-foreground mb-1 flex items-center gap-1"><FileJson className="h-3 w-3" /> Payload</p>
                                <pre className="bg-muted p-2 rounded-md text-xs overflow-x-auto font-mono">
                                    {tryFormatJson(job.payload)}
                                </pre>
                            </div>

                            {job.status === 'PENDING' && (
                                <div className="flex justify-end pt-2">
                                    <Button variant="destructive" size="sm" onClick={handleCancel}>
                                        Cancel Job
                                    </Button>
                                </div>
                            )}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </Card>
    );
};

const getStatusColor = (status) => {
    switch (status) {
        case 'PENDING': return '#eab308'; // yellow-500
        case 'RUNNING': return '#3b82f6'; // blue-500
        case 'COMPLETED': return '#22c55e'; // green-500
        case 'FAILED': return '#ef4444'; // red-500
        case 'CANCELLED': return '#6b7280'; // gray-500
        default: return '#cbd5e1';
    }
}

const tryFormatJson = (str) => {
    try {
        if (!str) return "null";
        const obj = JSON.parse(str);
        return JSON.stringify(obj, null, 2);
    } catch (e) {
        return str;
    }
}

export default JobCard;
