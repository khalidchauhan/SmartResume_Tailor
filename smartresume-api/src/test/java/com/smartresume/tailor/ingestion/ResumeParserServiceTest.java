package com.smartresume.tailor.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.ingestion.service.ResumeParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ResumeParserServiceTest {

    private ResumeParserService parserService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parserService = new ResumeParserService(objectMapper);
    }

    @Test
    @DisplayName("Should extract contact details, detected skills, and experience items from resume text")
    void testParseFromRawText() {
        String sampleText = """
                Jane Doe
                jane.doe@example.com | +1 (555) 987-6543 | New York, NY
                Staff Distributed Systems Architect
                
                Summary:
                Experienced distributed systems engineer with 10+ years specializing in Java 21,
                Kafka, Kubernetes, and high-concurrency microservices on AWS.
                
                Skills:
                Java, Java 21, Spring Boot, Kafka, PostgreSQL, Docker, Kubernetes, AWS, Redis, Microservices
                
                Experience:
                Tech Corp — Staff Engineer (2021 - Present)
                - Architected high-throughput ledger processing 150k RPM.
                - Reduced p99 query latency by 45% using Redis caching and PostgreSQL query tuning.
                """;

        ParsedResumeDto parsed = parserService.parseFromRawText(sampleText, "Jane_Doe_Resume.txt");

        assertNotNull(parsed);
        assertEquals("Jane Doe", parsed.getCandidateName());
        assertEquals("jane.doe@example.com", parsed.getEmail());
        assertTrue(parsed.getSkills().contains("Java"));
        assertTrue(parsed.getSkills().contains("Spring Boot"));
        assertTrue(parsed.getSkills().contains("Kafka"));
        assertTrue(parsed.getSkills().contains("Kubernetes"));
        assertNotNull(parsed.getExperience());
        assertFalse(parsed.getExperience().isEmpty());
    }

    @Test
    @DisplayName("Should parse multipart resume file upload through Apache Tika")
    void testParseMultipartFile() {
        String content = "Alex Mercer\nalex.mercer@example.com\nSenior Backend Engineer\nSkills: Java, PostgreSQL, Docker, AWS";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Alex_Resume.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        ParsedResumeDto parsed = parserService.parseResume(file);
        assertNotNull(parsed);
        assertEquals("alex.mercer@example.com", parsed.getEmail());
        assertTrue(parsed.getSkills().contains("Java"));
        assertTrue(parsed.getSkills().contains("PostgreSQL"));
    }
}
