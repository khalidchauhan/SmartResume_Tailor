package com.smartresume.tailor.ingestion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.model.JobIngestRequest;
import com.smartresume.tailor.domain.model.ParsedJobDto;
import com.smartresume.tailor.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobIngestionService {

    private final JobPostingRepository jobPostingRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<JobPosting> ingestJobs(JobIngestRequest request) {
        log.info("Ingesting jobs for keywords: {}, locations: {}", request.getKeywords(), request.getLocations());
        List<JobPosting> ingested = new ArrayList<>();

        // Curated set of realistic industry postings matching the engineering specs
        List<MockJobSpec> catalog = List.of(
                new MockJobSpec(
                        "job-stripe-001",
                        "Stripe",
                        "Lead Java Backend Engineer - Distributed Systems",
                        "San Francisco, CA / Remote",
                        true,
                        "Fintech",
                        "Staff",
                        7.0,
                        List.of("Java", "Java 21", "Spring Boot", "Kafka", "PostgreSQL", "Distributed Systems", "AWS", "Microservices"),
                        List.of("gRPC", "Docker", "Kubernetes"),
                        """
                        Stripe is looking for a Lead Java Backend Engineer to scale our core ledger and transaction pipeline.
                        You will architect mission-critical, low-latency microservices using Java 21, Spring Boot, and Apache Kafka.
                        Requirements:
                        - 7+ years building high-throughput distributed systems in Java/Spring.
                        - Deep experience with relational databases (PostgreSQL), index optimization, and distributed caching.
                        - Experience with event-driven message queues (Kafka) and cloud deployments on AWS.
                        - Passion for financial ledger correctness and sub-50ms latency.
                        """,
                        new BigDecimal("195000"),
                        new BigDecimal("265000")
                ),
                new MockJobSpec(
                        "job-datadog-002",
                        "Datadog",
                        "Staff Software Engineer - Cloud Platform",
                        "New York, NY / Remote",
                        true,
                        "Cloud Infrastructure",
                        "Staff",
                        8.0,
                        List.of("Java", "Kubernetes", "AWS", "Docker", "Distributed Systems", "Microservices"),
                        List.of("Go", "Terraform", "Kafka"),
                        """
                        Datadog seeks a Staff Software Engineer to expand our telemetry ingestion platforms.
                        Requirements:
                        - 8+ years hands-on engineering experience in Java or Go.
                        - Proven track record scaling containerized services on Kubernetes (EKS) and AWS.
                        - Strong distributed systems fundamentals: fault tolerance, consistency, and observability.
                        """,
                        new BigDecimal("210000"),
                        new BigDecimal("280000")
                ),
                new MockJobSpec(
                        "job-snowflake-003",
                        "Snowflake",
                        "Senior Backend Infrastructure Engineer",
                        "San Mateo, CA",
                        false,
                        "Data Infrastructure",
                        "Senior",
                        6.0,
                        List.of("Java", "PostgreSQL", "Distributed Systems", "REST", "CI/CD"),
                        List.of("C++", "Python"),
                        """
                        Join Snowflake's Core Data Lakehouse backend team.
                        Requirements:
                        - 6+ years experience with Java backends, database query engines, and distributed storage.
                        - Strong knowledge of concurrency, garbage collection tuning, and memory management.
                        """,
                        new BigDecimal("180000"),
                        new BigDecimal("240000")
                ),
                new MockJobSpec(
                        "job-vercel-004",
                        "Vercel",
                        "Senior Fullstack Engineer - React & Node",
                        "Remote",
                        true,
                        "Frontend Developer Tools",
                        "Senior",
                        5.0,
                        List.of("React", "Next.js", "TypeScript", "Node.js", "GraphQL"),
                        List.of("Tailwind CSS", "Rust"),
                        """
                        Vercel is looking for a Senior Fullstack Engineer to craft developer workflows.
                        Requirements:
                        - 5+ years building modern web applications with React, Next.js, and TypeScript.
                        - Deep knowledge of frontend performance, SSR, and GraphQL APIs.
                        - Minimal backend Java experience required; focus is Node.js and TypeScript runtime.
                        """,
                        new BigDecimal("160000"),
                        new BigDecimal("220000")
                )
        );

        for (MockJobSpec spec : catalog) {
            jobPostingRepository.findByExternalIdAndSource(spec.externalId(), "JOB_ENGINE")
                    .ifPresentOrElse(
                            existing -> ingested.add(existing),
                            () -> {
                                try {
                                    ParsedJobDto parsed = ParsedJobDto.builder()
                                            .title(spec.title())
                                            .company(spec.company())
                                            .location(spec.location())
                                            .isRemote(spec.isRemote())
                                            .domain(spec.domain())
                                            .seniorityLevel(spec.seniorityLevel())
                                            .minYearsExperience(spec.minYears())
                                            .requiredSkills(spec.requiredSkills())
                                            .niceToHaveSkills(spec.niceToHave())
                                            .build();

                                    JobPosting posting = JobPosting.builder()
                                            .externalId(spec.externalId())
                                            .source("JOB_ENGINE")
                                            .title(spec.title())
                                            .company(spec.company())
                                            .location(spec.location())
                                            .isRemote(spec.isRemote())
                                            .rawDescription(spec.description())
                                            .parsedRequirements(objectMapper.writeValueAsString(parsed))
                                            .salaryMin(spec.salaryMin())
                                            .salaryMax(spec.salaryMax())
                                            .currency("USD")
                                            .postedAt(Instant.now().minusSeconds(86400 * 2))
                                            .build();

                                    ingested.add(jobPostingRepository.save(posting));
                                } catch (Exception e) {
                                    log.error("Failed to save job posting: {}", spec.title(), e);
                                }
                            }
                    );
        }

        return ingested;
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    private record MockJobSpec(
            String externalId,
            String company,
            String title,
            String location,
            boolean isRemote,
            String domain,
            String seniorityLevel,
            Double minYears,
            List<String> requiredSkills,
            List<String> niceToHave,
            String description,
            BigDecimal salaryMin,
            BigDecimal salaryMax
    ) {}
}
