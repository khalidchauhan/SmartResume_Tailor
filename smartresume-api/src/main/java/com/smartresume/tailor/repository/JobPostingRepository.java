package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    List<JobPosting> findAllByOrderByCreatedAtDesc();
    Optional<JobPosting> findByExternalIdAndSource(String externalId, String source);
}
