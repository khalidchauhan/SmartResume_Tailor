package com.smartresume.tailor.domain.entity;

import com.smartresume.tailor.domain.enums.TailorStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tailored_resumes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailoredResume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "match_id", unique = true, nullable = false)
    private JobMatch jobMatch;

    @Lob
    @Column(name = "tailored_json", nullable = false, columnDefinition = "CLOB")
    private String tailoredJson;

    @Lob
    @Column(name = "diff_json", nullable = false, columnDefinition = "CLOB")
    private String diffJson;

    @Column(name = "ats_score")
    private Integer atsScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    @Builder.Default
    private TailorStatus status = TailorStatus.PENDING;

    @Column(name = "pdf_export_url")
    private String pdfExportUrl;

    @Column(name = "docx_export_url")
    private String docxExportUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
