package com.smartresume.tailor.matcher.controller;

import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.EvaluateMatchRequest;
import com.smartresume.tailor.matcher.service.SemanticMatcherService;
import com.smartresume.tailor.repository.BaseResumeRepository;
import com.smartresume.tailor.repository.JobMatchRepository;
import com.smartresume.tailor.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final SemanticMatcherService semanticMatcherService;
    private final JobMatchRepository jobMatchRepository;
    private final BaseResumeRepository baseResumeRepository;
    private final JobPostingRepository jobPostingRepository;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<JobMatch>> evaluateMatch(@RequestBody EvaluateMatchRequest request) {
        BaseResume resume = baseResumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + request.getResumeId()));
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + request.getJobId()));

        JobMatch match = semanticMatcherService.evaluateAndScore(resume, job);
        return ResponseEntity.ok(ApiResponse.ok(match));
    }

    @PostMapping("/evaluate-all")
    public ResponseEntity<ApiResponse<List<JobMatch>>> evaluateAllJobs(
            @RequestParam(value = "resumeId", required = false) UUID resumeId) {
        BaseResume resume = (resumeId != null)
                ? baseResumeRepository.findById(resumeId).orElseThrow(() -> new IllegalArgumentException("Resume not found"))
                : baseResumeRepository.findFirstByOrderByCreatedAtDesc().orElseThrow(() -> new IllegalStateException("No resumes uploaded yet"));

        List<JobPosting> jobs = jobPostingRepository.findAll();
        List<JobMatch> results = new ArrayList<>();

        for (JobPosting job : jobs) {
            JobMatch match = semanticMatcherService.evaluateAndScore(resume, job);
            results.add(match);
        }

        results.sort(Comparator.comparingInt(JobMatch::getOverallScore).reversed());
        return ResponseEntity.ok(ApiResponse.ok(results, String.format("Evaluated %d jobs against resume", results.size())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobMatch>>> getAllMatches(
            @RequestParam(value = "status", required = false) MatchStatus status) {
        List<JobMatch> matches = (status != null)
                ? jobMatchRepository.findByStatusOrderByOverallScoreDesc(status)
                : jobMatchRepository.findAll();
        matches.sort(Comparator.comparingInt(JobMatch::getOverallScore).reversed());
        return ResponseEntity.ok(ApiResponse.ok(matches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobMatch>> getMatchById(@PathVariable UUID id) {
        return jobMatchRepository.findById(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok(m)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMatchStats() {
        long totalMatches = jobMatchRepository.count();
        long qualifiedCount = jobMatchRepository.countByStatus(MatchStatus.QUALIFIED);
        long droppedCount = jobMatchRepository.countByStatus(MatchStatus.DROPPED_LOW_MATCH);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalMatches", totalMatches);
        stats.put("qualifiedCount", qualifiedCount);
        stats.put("droppedCount", droppedCount);
        stats.put("qualificationThreshold", 80);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
