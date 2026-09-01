package com.smartresume.tailor.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id")
    private String externalId;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Column(name = "is_remote")
    @Builder.Default
    private Boolean isRemote = false;

    @Lob
    @Column(name = "raw_description", nullable = false, columnDefinition = "CLOB")
    private String rawDescription;

    @Lob
    @Column(name = "parsed_requirements", columnDefinition = "CLOB")
    private String parsedRequirements;

    @Column(name = "salary_min")
    private BigDecimal salaryMin;

    @Column(name = "salary_max")
    private BigDecimal salaryMax;

    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "posted_at")
    private Instant postedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
