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
