package com.smartresume.tailor.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.JobIngestRequest;
import com.smartresume.tailor.ingestion.controller.JobController;
import com.smartresume.tailor.ingestion.service.JobIngestionService;
import com.smartresume.tailor.repository.InMemoryJobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobControllerTest {

    private JobController jobController;
    private JobIngestionService ingestionService;
    private InMemoryJobPostingRepository repository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        repository = new InMemoryJobPostingRepository();
        ingestionService = new JobIngestionService(repository, objectMapper);
        jobController = new JobController(ingestionService);
    }

    @Test
    @DisplayName("Should accept job ingestion request and return ingested jobs")
    void testIngestJobs() {
        JobIngestRequest request = JobIngestRequest.builder()
                .keywords(List.of("Java", "Distributed Systems"))
                .locations(List.of("Remote"))
                .build();

        ResponseEntity<ApiResponse<List<JobPosting>>> response = jobController.ingestJobs(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertFalse(response.getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("Should retrieve list of all stored jobs")
    void testGetJobs() {
        jobController.ingestJobs(null);

        ResponseEntity<ApiResponse<List<JobPosting>>> response = jobController.getJobs();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getData().isEmpty());
    }
}
