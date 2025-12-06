CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    concurrent_job_limit INTEGER DEFAULT 5,
    rate_limit INTEGER DEFAULT 10,
    created_at TIMESTAMP
);

CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    payload TEXT,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    trace_id VARCHAR(255),
    created_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    version INTEGER
);

CREATE TABLE dlq_entries (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    failure_reason TEXT,
    failed_at TIMESTAMP,
    original_payload TEXT,
    CONSTRAINT fk_dlq_job FOREIGN KEY (job_id) REFERENCES jobs(id)
);

CREATE INDEX idx_job_tenant_id ON jobs(tenant_id);
CREATE INDEX idx_job_status ON jobs(status);
CREATE INDEX idx_job_idempotency_key ON jobs(idempotency_key);
CREATE INDEX idx_job_created_at ON jobs(created_at);
