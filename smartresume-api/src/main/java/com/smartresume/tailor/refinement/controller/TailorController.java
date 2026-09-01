package com.smartresume.tailor.refinement.controller;

import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.refinement.service.ResumeRefinementService;
import com.smartresume.tailor.repository.JobMatchRepository;
import com.smartresume.tailor.repository.TailoredResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tailor")
@RequiredArgsConstructor
@Slf4j
public class TailorController {

    private final ResumeRefinementService resumeRefinementService;
    private final JobMatchRepository jobMatchRepository;
    private final TailoredResumeRepository tailoredResumeRepository;

    @PostMapping("/{matchId}/generate")
    public ResponseEntity<ApiResponse<TailoredResume>> generateTailoredResume(@PathVariable UUID matchId) {
        JobMatch match = jobMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Job match not found: " + matchId));

        if (match.getStatus() != MatchStatus.QUALIFIED || match.getOverallScore() < 80) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(
                    String.format("Job match score (%d%%) is below the 80%% threshold. Refinement is not permitted.",
                            match.getOverallScore())));
        }

        TailoredResume tailored = resumeRefinementService.refineResume(match);
        return ResponseEntity.ok(ApiResponse.ok(tailored, "Resume successfully tailored for target job"));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<TailoredResume>> getTailoredByMatchId(@PathVariable UUID matchId) {
        return tailoredResumeRepository.findByJobMatchId(matchId)
                .map(t -> ResponseEntity.ok(ApiResponse.ok(t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No tailored resume found for match ID: " + matchId)));
    }

    @GetMapping("/item/{tailoredResumeId}")
    public ResponseEntity<ApiResponse<TailoredResume>> getTailoredById(@PathVariable UUID tailoredResumeId) {
        return tailoredResumeRepository.findById(tailoredResumeId)
                .map(t -> ResponseEntity.ok(ApiResponse.ok(t)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Tailored resume not found: " + tailoredResumeId)));
    }
}
