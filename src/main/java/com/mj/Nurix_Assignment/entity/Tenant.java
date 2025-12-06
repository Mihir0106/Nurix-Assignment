package com.mj.Nurix_Assignment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    private Integer concurrentJobLimit = 5;

    @Builder.Default
    private Integer rateLimit = 10;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;
}
