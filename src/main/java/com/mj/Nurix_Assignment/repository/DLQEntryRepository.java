package com.mj.Nurix_Assignment.repository;

import com.mj.Nurix_Assignment.entity.DLQEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DLQEntryRepository extends JpaRepository<DLQEntry, UUID> {
}
