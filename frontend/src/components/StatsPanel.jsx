import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useJobs } from '@/context/JobContext';
import { Activity, CheckCircle, AlertOctagon, Layers } from 'lucide-react';

const StatCard = ({ title, value, icon: Icon, description, colorClass }) => (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
                {title}
            </CardTitle>
            <Icon className={`h-4 w-4 text-muted-foreground ${colorClass}`} />
        </CardHeader>
        <CardContent>
            <div className="text-2xl font-bold">{value}</div>
            <p className="text-xs text-muted-foreground">
                {description}
            </p>
        </CardContent>
    </Card>
);

const StatsPanel = () => {
    const { stats } = useJobs();

    // Calculate success rate
    const totalFinished = stats.completed + stats.failed;
    const successRate = totalFinished > 0
        ? Math.round((stats.completed / totalFinished) * 100)
        : 0;

    return (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
                title="Total Jobs"
                value={stats.total}
                icon={Layers}
                description="All time"
            />
            <StatCard
                title="Active"
                value={stats.running + stats.pending}
                icon={Activity}
                description="Running & Pending"
                colorClass="text-blue-500"
            />
            <StatCard
                title="Success Rate"
                value={`${successRate}%`}
                icon={CheckCircle}
                description={`${stats.completed} completed`}
                colorClass="text-green-500"
            />
            <StatCard
                title="Failed"
                value={stats.failed}
                icon={AlertOctagon}
                description="Total failures"
                colorClass="text-red-500"
            />
        </div>
    );
};

export default StatsPanel;
