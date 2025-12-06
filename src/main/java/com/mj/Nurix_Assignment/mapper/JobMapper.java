package com.mj.Nurix_Assignment.mapper;

import com.mj.Nurix_Assignment.dto.JobResponse;
import com.mj.Nurix_Assignment.dto.JobStatusResponse;
import com.mj.Nurix_Assignment.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * MapStruct mapper for converting between Job entity and DTOs.
 * Uses Mapper Pattern for object transformation.
 */
@Mapper(componentModel = "spring")
public interface JobMapper {

    JobMapper INSTANCE = Mappers.getMapper(JobMapper.class);

    @Mapping(target = "createdAt", expression = "java(toInstant(job.getCreatedAt()))")
    @Mapping(target = "startedAt", expression = "java(toInstant(job.getStartedAt()))")
    @Mapping(target = "completedAt", expression = "java(toInstant(job.getCompletedAt()))")
    JobResponse toResponse(Job job);

    @Mapping(target = "progress", expression = "java(calculateProgress(job))")
    JobStatusResponse toStatusResponse(Job job);

    default Instant toInstant(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Integer calculateProgress(Job job) {
        // Simple progress calculation - can be enhanced based on business logic
        if (job.getStatus() == null) {
            return null;
        }
        return switch (job.getStatus()) {
            case PENDING -> 0;
            case RUNNING -> 50;
            case COMPLETED -> 100;
            case FAILED, CANCELLED -> null;
        };
    }
}

