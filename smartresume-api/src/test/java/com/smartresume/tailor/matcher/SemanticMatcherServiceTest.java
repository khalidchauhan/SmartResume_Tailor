package com.smartresume.tailor.matcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.model.MatchEvaluationDto;
import com.smartresume.tailor.domain.model.ParsedJobDto;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.matcher.service.SemanticMatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticMatcherServiceTest {

    private ObjectMapper objectMapper;
    private SemanticMatcherService matcherService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        matcherService = new SemanticMatcherService(null, objectMapper);
    }

    @Test
    @DisplayName("Should qualify candidate with score >= 80% when skills and experience align closely")
    void testCandidateQualifiesForLeadJavaRole() {
        ParsedResumeDto resumeDto = ParsedResumeDto.builder()
                .candidateName("Alex Mercer")
                .headline("Senior Backend & Distributed Systems Engineer")
                .summary("Experienced software engineer with 8+ years building high-scale distributed backend systems on AWS using Java and Spring Boot.")
                .yearsOfExperience(8.0)
                .skills(List.of("Java", "Java 21", "Spring Boot", "Kafka", "PostgreSQL", "AWS", "Kubernetes", "Docker", "Microservices"))
                .build();

        ParsedJobDto highMatchJob = ParsedJobDto.builder()
                .title("Lead Java Backend Engineer")
                .company("Stripe")
                .domain("Fintech")
                .minYearsExperience(7.0)
                .requiredSkills(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL", "AWS", "Distributed Systems"))
                .build();

        MatchEvaluationDto evaluation = matcherService.computeDeterministicScore(resumeDto, highMatchJob);

        assertNotNull(evaluation);
        assertTrue(evaluation.getOverallScore() >= 80, "Expected score >= 80% but was " + evaluation.getOverallScore());
        assertEquals("QUALIFIED_FOR_REFINEMENT", evaluation.getVerdict());
        assertTrue(evaluation.getMatchedSkills().contains("Java"));
        assertTrue(evaluation.getMatchedSkills().contains("Spring Boot"));
    }

    @Test
    @DisplayName("Should drop and archive job with score < 80% when role has deep tech mismatch")
    void testCandidateDroppedForMismatchedFrontendRole() {
        ParsedResumeDto resumeDto = ParsedResumeDto.builder()
                .candidateName("Alex Mercer")
                .headline("Senior Backend Engineer")
                .summary("Backend Java engineer focused on microservices and database engines.")
                .yearsOfExperience(8.0)
                .skills(List.of("Java", "Spring Boot", "PostgreSQL", "Kafka"))
                .build();

        ParsedJobDto frontendJob = ParsedJobDto.builder()
                .title("Senior Fullstack Engineer - React & Node")
                .company("Vercel")
                .domain("Frontend Developer Tools")
                .minYearsExperience(5.0)
                .requiredSkills(List.of("React", "Next.js", "TypeScript", "Node.js", "GraphQL", "Tailwind CSS"))
                .build();

        MatchEvaluationDto evaluation = matcherService.computeDeterministicScore(resumeDto, frontendJob);

        assertNotNull(evaluation);
        assertTrue(evaluation.getOverallScore() < 80, "Expected score < 80% for mismatched role but was " + evaluation.getOverallScore());
        assertEquals("DROPPED_LOW_MATCH", evaluation.getVerdict());
        assertTrue(evaluation.getVerdictReason().contains("falls below 80% threshold"));
        assertFalse(evaluation.getCriticalMissingSkills().isEmpty());
    }
}
