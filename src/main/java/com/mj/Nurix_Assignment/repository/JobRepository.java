package com.mj.Nurix_Assignment.repository;

import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    // Custom query to find pending jobs by tenant
    List<Job> findByTenantIdAndStatus(String tenantId, JobStatus status);

    // Custom query to count running jobs by tenant
    long countByTenantIdAndStatus(String tenantId, JobStatus status);

    // Global count by status
    long countByStatus(JobStatus status);

    // Find by unique idempotency key
    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    // Find first pending job for tenant to preserve FIFO/ordering if needed
    Optional<Job> findFirstByTenantIdAndStatusOrderByCreatedAtAsc(String tenantId, JobStatus status);
}
