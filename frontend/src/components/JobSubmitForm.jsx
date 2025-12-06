import React, { useState } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useJobs } from '@/context/JobContext';
import { Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';

const JobSubmitForm = () => {
    const { submitJob } = useJobs();
    const [payload, setPayload] = useState('{\n  "task": "example-task",\n  "priority": "high"\n}');
    const [idempotencyKey, setIdempotencyKey] = useState('');
    const [maxRetries, setMaxRetries] = useState(3);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validate JSON
        let parsedPayload;
        try {
            parsedPayload = JSON.parse(payload);
        } catch (err) {
            toast.error("Invalid JSON payload");
            return;
        }

        setLoading(true);
        try {
            await submitJob(parsedPayload, idempotencyKey || undefined, parseInt(maxRetries));
            // Reset form partly?
            setIdempotencyKey('');
        } catch (err) {
            // Handled in context
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Submit New Job</CardTitle>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <label className="text-sm font-medium">Payload (JSON)</label>
                        <textarea
                            value={payload}
                            onChange={(e) => setPayload(e.target.value)}
                            className="w-full h-32 p-2 rounded-md border bg-background font-mono text-sm resize-none focus:outline-none focus:ring-2 focus:ring-ring"
                            placeholder="{ ... }"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="text-sm font-medium">Idempotency Key (Optional)</label>
                            <input
                                type="text"
                                value={idempotencyKey}
                                onChange={(e) => setIdempotencyKey(e.target.value)}
                                className="w-full h-10 px-3 rounded-md border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                                placeholder="UUID or unique string"
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium">Max Retries</label>
                            <input
                                type="number"
                                value={maxRetries}
                                onChange={(e) => setMaxRetries(e.target.value)}
                                min="0" max="10"
                                className="w-full h-10 px-3 rounded-md border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                            />
                        </div>
                    </div>

                    <Button type="submit" className="w-full" disabled={loading}>
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Submit Job
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
};

export default JobSubmitForm;
