package com.smartresume.tailor.refinement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.refinement.controller.TailorController;
import com.smartresume.tailor.refinement.service.ResumeRefinementService;
import com.smartresume.tailor.repository.InMemoryJobMatchRepository;
import com.smartresume.tailor.repository.InMemoryTailoredResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TailorControllerTest {

    private TailorController tailorController;
    private InMemoryJobMatchRepository matchRepository;
    private InMemoryTailoredResumeRepository tailoredRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        matchRepository = new InMemoryJobMatchRepository();
        tailoredRepository = new InMemoryTailoredResumeRepository();

        ResumeRefinementService refinementService = new ResumeRefinementService(tailoredRepository, objectMapper);
        tailorController = new TailorController(refinementService, matchRepository, tailoredRepository);
    }

    @Test
    @DisplayName("Should forbid refinement if match score is below 80%")
    void testForbidsRefinementForLowMatch() {
        JobMatch lowMatch = JobMatch.builder()
                .overallScore(60)
                .status(MatchStatus.DROPPED_LOW_MATCH)
                .build();
        matchRepository.save(lowMatch);

        ResponseEntity<ApiResponse<TailoredResume>> response = tailorController.generateTailoredResume(lowMatch.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("below the 80% threshold"));
    }

    @Test
    @DisplayName("Should successfully tailor resume for qualified matches >= 80%")
    void testTailorsQualifiedMatch() throws Exception {
        ParsedResumeDto resumeDto = ParsedResumeDto.builder()
                .candidateName("Alex Mercer")
                .skills(List.of("Java 21", "Spring Boot", "Kafka", "AWS"))
                .yearsOfExperience(8.0)
                .build();
        BaseResume resume = BaseResume.builder()
                .fileName("Alex_Resume.pdf")
                .rawText("Sample raw text")
                .parsedJson(objectMapper.writeValueAsString(resumeDto))
                .build();

        JobPosting job = JobPosting.builder()
                .title("Lead Java Engineer")
                .company("Stripe")
                .rawDescription("Stripe is looking for a Lead Java Backend Engineer...")
                .build();

        JobMatch qualifiedMatch = JobMatch.builder()
                .resume(resume)
                .job(job)
                .overallScore(88)
                .status(MatchStatus.QUALIFIED)
                .build();
        matchRepository.save(qualifiedMatch);

        ResponseEntity<ApiResponse<TailoredResume>> response = tailorController.generateTailoredResume(qualifiedMatch.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().getAtsScore() >= 90);
    }
}
