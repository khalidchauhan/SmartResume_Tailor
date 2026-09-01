package com.smartresume.tailor.refinement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.refinement.service.ResumeRefinementService;
import com.smartresume.tailor.repository.InMemoryTailoredResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResumeRefinementServiceTest {

    private ObjectMapper objectMapper;
    private ResumeRefinementService refinementService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        InMemoryTailoredResumeRepository fakeRepo = new InMemoryTailoredResumeRepository();
        refinementService = new ResumeRefinementService(fakeRepo, objectMapper);
    }

    @Test
    @DisplayName("Should throw IllegalStateException if attempting to refine a match below 80%")
    void testDisallowsRefiningLowMatch() {
        JobMatch lowMatch = JobMatch.builder()
                .id(UUID.randomUUID())
                .overallScore(65)
                .status(MatchStatus.DROPPED_LOW_MATCH)
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            refinementService.refineResume(lowMatch);
        });

        assertTrue(ex.getMessage().contains("below the required 80% threshold"));
    }

    @Test
    @DisplayName("Should successfully refine resume and generate diffs for matches >= 80%")
    void testRefinesQualifiedMatch() throws Exception {
        ParsedResumeDto resumeDto = ParsedResumeDto.builder()
                .candidateName("Alex Mercer")
                .email("alex.mercer@example.com")
                .summary("Experienced software engineer with 8 years building backend systems.")
                .yearsOfExperience(8.0)
                .experience(List.of(
                        ParsedResumeDto.ExperienceItem.builder()
                                .company("Tech Corp")
                                .role("Senior Software Engineer")
                                .bullets(List.of("Architected event-driven microservices processing 120k RPM."))
                                .build()
                ))
                .build();

        BaseResume resume = BaseResume.builder()
                .id(UUID.randomUUID())
                .parsedJson(objectMapper.writeValueAsString(resumeDto))
                .build();

        JobPosting job = JobPosting.builder()
                .id(UUID.randomUUID())
                .title("Lead Java Systems Engineer")
                .company("Stripe")
                .build();

        JobMatch qualifiedMatch = JobMatch.builder()
                .id(UUID.randomUUID())
                .resume(resume)
                .job(job)
                .overallScore(86)
                .status(MatchStatus.QUALIFIED)
                .build();

        TailoredResume tailored = refinementService.refineResume(qualifiedMatch);

        assertNotNull(tailored);
        assertNotNull(tailored.getTailoredJson());
        assertNotNull(tailored.getDiffJson());
        assertTrue(tailored.getAtsScore() >= 90);
        assertTrue(tailored.getTailoredJson().contains("Staff Distributed Systems Engineer"));
    }
}
