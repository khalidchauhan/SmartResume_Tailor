package com.smartresume.tailor.matcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.ParsedJobDto;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.matcher.controller.MatchController;
import com.smartresume.tailor.matcher.service.SemanticMatcherService;
import com.smartresume.tailor.repository.InMemoryBaseResumeRepository;
import com.smartresume.tailor.repository.InMemoryJobMatchRepository;
import com.smartresume.tailor.repository.InMemoryJobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchControllerTest {

    private MatchController matchController;
    private InMemoryJobMatchRepository matchRepository;
    private InMemoryBaseResumeRepository resumeRepository;
    private InMemoryJobPostingRepository jobRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        matchRepository = new InMemoryJobMatchRepository();
        resumeRepository = new InMemoryBaseResumeRepository();
        jobRepository = new InMemoryJobPostingRepository();

        SemanticMatcherService matcherService = new SemanticMatcherService(matchRepository, objectMapper);
        matchController = new MatchController(matcherService, matchRepository, resumeRepository, jobRepository);

        ParsedResumeDto resumeDto = ParsedResumeDto.builder()
                .candidateName("Alex Mercer")
                .skills(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL", "AWS"))
                .yearsOfExperience(8.0)
                .build();
        BaseResume resume = BaseResume.builder()
                .fileName("Alex_Resume.pdf")
                .rawText("Sample raw text")
                .parsedJson(objectMapper.writeValueAsString(resumeDto))
                .build();
        resumeRepository.save(resume);

        ParsedJobDto job1 = ParsedJobDto.builder()
                .title("Lead Java Engineer")
                .company("Stripe")
                .requiredSkills(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL", "AWS"))
                .minYearsExperience(7.0)
                .build();
        jobRepository.save(JobPosting.builder()
                .source("TEST")
                .title("Lead Java Engineer")
                .company("Stripe")
                .rawDescription("Lead Java role")
                .parsedRequirements(objectMapper.writeValueAsString(job1))
                .build());

        ParsedJobDto job2 = ParsedJobDto.builder()
                .title("Senior Frontend Dev")
                .company("Vercel")
                .requiredSkills(List.of("React", "Next.js", "TypeScript"))
                .minYearsExperience(5.0)
                .build();
        jobRepository.save(JobPosting.builder()
                .source("TEST")
                .title("Senior Frontend Dev")
                .company("Vercel")
                .rawDescription("Frontend role")
                .parsedRequirements(objectMapper.writeValueAsString(job2))
                .build());
    }

    @Test
    @DisplayName("Should evaluate all jobs against active resume and classify scores correctly")
    void testEvaluateAllJobs() {
        ResponseEntity<ApiResponse<List<JobMatch>>> response = matchController.evaluateAllJobs(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<JobMatch> matches = response.getBody().getData();
        assertEquals(2, matches.size());

        assertTrue(matches.stream().anyMatch(m -> "Stripe".equals(m.getJob().getCompany()) && m.getStatus() == MatchStatus.QUALIFIED));
        assertTrue(matches.stream().anyMatch(m -> "Vercel".equals(m.getJob().getCompany()) && m.getStatus() == MatchStatus.DROPPED_LOW_MATCH));
    }

    @Test
    @DisplayName("Should return pipeline stats matching threshold logic")
    void testGetMatchStats() {
        matchController.evaluateAllJobs(null);

        ResponseEntity<ApiResponse<Map<String, Object>>> statsResponse = matchController.getMatchStats();
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        Map<String, Object> stats = statsResponse.getBody().getData();

        assertEquals(2L, stats.get("totalMatches"));
        assertEquals(1L, stats.get("qualifiedCount"));
        assertEquals(1L, stats.get("droppedCount"));
        assertEquals(80, stats.get("qualificationThreshold"));
    }
}
