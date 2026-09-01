package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.TailoredResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TailoredResumeRepository extends JpaRepository<TailoredResume, UUID> {
    Optional<TailoredResume> findByJobMatchId(UUID matchId);
}
