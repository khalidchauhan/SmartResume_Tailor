package com.smartresume.tailor.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.ingestion.controller.ResumeController;
import com.smartresume.tailor.ingestion.service.ResumeParserService;
import com.smartresume.tailor.repository.InMemoryBaseResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ResumeControllerTest {

    private ResumeController resumeController;
    private InMemoryBaseResumeRepository resumeRepository;
    private ResumeParserService parserService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        resumeRepository = new InMemoryBaseResumeRepository();
        parserService = new ResumeParserService(objectMapper);
        resumeController = new ResumeController(parserService, resumeRepository, objectMapper);
    }

    @Test
    @DisplayName("Should load sample Senior SWE resume")
    void testLoadSampleResume() {
        ResponseEntity<ApiResponse<BaseResume>> response = resumeController.loadSampleResume();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData().getId());
        assertEquals("Alex_Mercer_Senior_SWE.pdf", response.getBody().getData().getFileName());
    }

    @Test
    @DisplayName("Should upload and parse resume file")
    void testUploadResume() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "custom_resume.pdf",
                "application/pdf",
                "John Developer\njohn@example.com\nStaff Architect\nSkills: Java, Kafka, AWS".getBytes(StandardCharsets.UTF_8)
        );

        ResponseEntity<ApiResponse<BaseResume>> response = resumeController.uploadResume(file, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("custom_resume.pdf", response.getBody().getData().getFileName());
    }

    @Test
    @DisplayName("Should fetch latest uploaded resume")
    void testGetLatestResume() {
        resumeController.loadSampleResume();

        ResponseEntity<ApiResponse<BaseResume>> response = resumeController.getLatestResume();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }
}
