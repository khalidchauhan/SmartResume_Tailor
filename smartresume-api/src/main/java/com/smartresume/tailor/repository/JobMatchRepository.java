package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, UUID> {
    List<JobMatch> findByResumeIdOrderByOverallScoreDesc(UUID resumeId);
    List<JobMatch> findByStatusOrderByOverallScoreDesc(MatchStatus status);
    Optional<JobMatch> findByResumeIdAndJobId(UUID resumeId, UUID jobId);
    long countByStatus(MatchStatus status);
}
