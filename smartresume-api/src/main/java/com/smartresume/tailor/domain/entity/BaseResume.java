package com.smartresume.tailor.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "base_resumes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "storage_url")
    private String storageUrl;

    @Lob
    @Column(name = "raw_text", nullable = false, columnDefinition = "CLOB")
    private String rawText;

    @Lob
    @Column(name = "parsed_json", nullable = false, columnDefinition = "CLOB")
    private String parsedJson;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
