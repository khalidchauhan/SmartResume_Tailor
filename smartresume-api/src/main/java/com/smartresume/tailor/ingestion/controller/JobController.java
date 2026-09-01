package com.smartresume.tailor.ingestion.controller;

import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.JobIngestRequest;
import com.smartresume.tailor.ingestion.service.JobIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobIngestionService jobIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<List<JobPosting>>> ingestJobs(@RequestBody(required = false) JobIngestRequest request) {
        if (request == null) {
            request = JobIngestRequest.builder()
                    .keywords(List.of("Java", "Distributed Systems", "Backend"))
                    .locations(List.of("Remote", "San Francisco"))
                    .build();
        }
        List<JobPosting> jobs = jobIngestionService.ingestJobs(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(jobs, String.format("Successfully ingested %d jobs", jobs.size())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobPosting>>> getJobs() {
        List<JobPosting> jobs = jobIngestionService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.ok(jobs));
    }
}
