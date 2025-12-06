# Distributed Task Queue System - Domain Layer

## Overview
This project implements the backend for a Distributed Task Queue System using Spring Boot 3.2 and Java 21 (compatible with 17+). It features a robust domain layer with JPA entities, repositories, and Flyway for database migrations.

## Low Level Design (LLD)

### Entities & Relationships
1.  **Job**: Represents a task to be executed.
    -   **Attributes**: `id`, `tenantId`, `payload`, `status`, `idempotencyKey`, `retryCount`, `maxRetries`, `traceId`, `timestamps`.
    -   **Relationships**: 
        -   One-to-Many with `DLQEntry` (logically, though DLQ references key).
2.  **Tenant**: Represents a client/tenant using the system.
    -   **Attributes**: `id`, `name`, `concurrentJobLimit`, `rateLimit`.
3.  **DLQEntry**: Dead Letter Queue for failed jobs.
    -   **Attributes**: `id`, `jobId`, `failureReason`, `failedAt`, `originalPayload`.
    -   **Relationships**: Many-to-One with `Job`.

### Database Schema
-   `jobs` table with indexes for high-performance querying on `tenant_id`, `status`, `idempotency_key`.
-   `tenants` table for configuration.
-   `dlq_entries` table for error audit.
-   Foreign Key constraint `fk_dlq_job` ensures data integrity between DLQ and Jobs.

## Worker System LLD

### Components
1.  **JobScheduler**:
    -   runs every 5 seconds (`@Scheduled`).
    -   Iterates through tenants to ensure fair polling.
    -   Checks `concurrentJobLimit` vs running jobs count before leasing.
    -   **Leasing**: Uses optimistic locking (`UPDATE ... WHERE version=?`) to mark a job as `RUNNING`.

2.  **JobExecutor**:
    -   `ThreadPoolTaskExecutor` (Core: 5, Max: 10, Queue: 100).
    -   Executes `JobProcessor` logic asynchronously.

3.  **JobProcessor**:
    -   Parses payload and simulates work (sleep 5-10s).
    -   Uses **MDC** to inject `traceId` into logs for observability.

4.  **RetryPolicy**:
    -   **Exponential Backoff**: `2 * 2^retryCount` seconds.
    -   Updates status to `PENDING` (scheduler will pick it up after delay logic, or immediately in current simple implementation).
    -   Moves to **DLQ** `dlq_entries` table if `maxRetries` exceeded.

## Design Patterns Used
1.  **Repository Pattern**:
    -   `JobRepository`, `TenantRepository`, `DLQEntryRepository` provide an abstraction over the data access layer, decoupling domain logic from database operations.
    -   Extends `JpaRepository` for standard CRUD and custom finders.

2.  **Builder Pattern**:
    -   Utilized Lombok's `@Builder` on all entities (`Job`, `Tenant`, `DLQEntry`) to provide a fluent API for object construction, improving readability and maintainability.

3.  **Entity Pattern (Domain Model)**:
    -   Rich domain objects annotated with `@Entity` map directly to database tables, encapsulating data and basic validation constraints.

4.  **Optimistic Locking**:
    -   Used `@Version` in `Job` entity to handle concurrent updates safely without explicit locking, crucial for a distributed system.

## Setup & Migration
-   **Flyway** is used for database version control.
-   Migration script: `src/main/resources/db/migration/V1__initial_schema.sql` creates the initial schema.
