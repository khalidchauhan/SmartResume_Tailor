package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.BaseResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BaseResumeRepository extends JpaRepository<BaseResume, UUID> {
    List<BaseResume> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<BaseResume> findFirstByOrderByCreatedAtDesc();
}
