package com.mj.Nurix_Assignment;

import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NurixAssignmentApplicationTests {

	@Autowired
	private JobRepository jobRepository;

	@Test
	void contextLoads() {
		assertThat(jobRepository).isNotNull();
	}

	@Test
	void testJobPersistence() {
		Job job = Job.builder()
				.tenantId("tenant-1")
				.payload("{\"action\":\"test\"}")
				.status(JobStatus.PENDING)
				.idempotencyKey(UUID.randomUUID().toString())
				.build();

		Job savedJob = jobRepository.save(job);

		assertThat(savedJob.getId()).isNotNull();
		assertThat(savedJob.getCreatedAt()).isNotNull();
		assertThat(savedJob.getStatus()).isEqualTo(JobStatus.PENDING);
	}
}
