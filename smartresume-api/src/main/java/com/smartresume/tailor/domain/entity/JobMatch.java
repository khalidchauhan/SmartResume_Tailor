package com.smartresume.tailor.domain.entity;

import com.smartresume.tailor.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_matches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private BaseResume resume;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting job;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @Column(name = "skills_score", nullable = false)
    private int skillsScore;

    @Column(name = "experience_score", nullable = false)
    private int experienceScore;

    @Column(name = "domain_score", nullable = false)
    private int domainScore;

    @Column(name = "education_score", nullable = false)
    private int educationScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MatchStatus status;

    @Column(name = "archive_reason")
    private String archiveReason;

    @Lob
    @Column(name = "evaluation_json", nullable = false, columnDefinition = "CLOB")
    private String evaluationJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
