package com.smartresume.tailor.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.model.JobIngestRequest;
import com.smartresume.tailor.ingestion.service.JobIngestionService;
import com.smartresume.tailor.repository.InMemoryJobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobIngestionServiceTest {

    private JobIngestionService ingestionService;
    private InMemoryJobPostingRepository jobPostingRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jobPostingRepository = new InMemoryJobPostingRepository();
        ingestionService = new JobIngestionService(jobPostingRepository, objectMapper);
    }

    @Test
    @DisplayName("Should ingest curated catalog of jobs including high-match and low-match tech roles")
    void testIngestJobs() {
        JobIngestRequest request = JobIngestRequest.builder()
                .keywords(List.of("Java", "Distributed Systems"))
                .locations(List.of("San Francisco", "Remote"))
                .limit(10)
                .build();

        List<JobPosting> ingested = ingestionService.ingestJobs(request);

        assertNotNull(ingested);
        assertFalse(ingested.isEmpty());
        assertTrue(ingested.stream().anyMatch(j -> "Stripe".equalsIgnoreCase(j.getCompany())));
        assertTrue(ingested.stream().anyMatch(j -> "Datadog".equalsIgnoreCase(j.getCompany())));
        assertTrue(ingested.stream().anyMatch(j -> "Vercel".equalsIgnoreCase(j.getCompany())));

        List<JobPosting> allJobs = ingestionService.getAllJobs();
        assertEquals(ingested.size(), allJobs.size());
    }

    @Test
    @DisplayName("Should prevent duplicate job postings on subsequent ingestion calls")
    void testDeduplicateJobs() {
        JobIngestRequest request = JobIngestRequest.builder().build();

        List<JobPosting> batch1 = ingestionService.ingestJobs(request);
        int count1 = batch1.size();

        List<JobPosting> batch2 = ingestionService.ingestJobs(request);
        assertEquals(count1, batch2.size());
        assertEquals(count1, jobPostingRepository.findAll().size());
    }
}
