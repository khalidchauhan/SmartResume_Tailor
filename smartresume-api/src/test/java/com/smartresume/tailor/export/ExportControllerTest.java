package com.smartresume.tailor.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.domain.model.TailoredResumeDto;
import com.smartresume.tailor.export.controller.ExportController;
import com.smartresume.tailor.export.service.DocumentExportService;
import com.smartresume.tailor.repository.InMemoryTailoredResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExportControllerTest {

    private ExportController exportController;
    private InMemoryTailoredResumeRepository tailoredRepository;
    private DocumentExportService exportService;
    private ObjectMapper objectMapper;
    private TailoredResume seededTailoredResume;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        tailoredRepository = new InMemoryTailoredResumeRepository();
        exportService = new DocumentExportService(objectMapper);
        exportController = new ExportController(exportService, tailoredRepository);

        TailoredResumeDto dto = TailoredResumeDto.builder()
                .candidateName("Alex Mercer")
                .email("alex@example.com")
                .phone("555-123-4567")
                .location("San Francisco, CA")
                .tailoredHeadline("Staff Distributed Systems Engineer")
                .tailoredSummary("Experienced software engineer scaling backends.")
                .skillsSection(Map.of("Languages", List.of("Java 21", "SQL")))
                .workExperience(List.of(
                        ParsedResumeDto.ExperienceItem.builder()
                                .company("Tech Corp")
                                .role("Senior Software Engineer")
                                .startDate("2021-03")
                                .endDate("Present")
                                .bullets(List.of("Architected event-driven microservices processing 120k RPM."))
                                .build()
                ))
                .build();

        JobPosting job = JobPosting.builder().title("Lead Java Engineer").company("Stripe").build();
        JobMatch match = JobMatch.builder().job(job).build();

        seededTailoredResume = TailoredResume.builder()
                .id(UUID.randomUUID())
                .jobMatch(match)
                .tailoredJson(objectMapper.writeValueAsString(dto))
                .atsScore(95)
                .build();
        tailoredRepository.save(seededTailoredResume);
    }

    @Test
    @DisplayName("Should export tailored resume as PDF binary stream with Content-Disposition header")
    void testExportPdf() {
        ResponseEntity<byte[]> response = exportController.exportPdf(seededTailoredResume.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment; filename=\"Tailored_Resume_Stripe.pdf\""));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 500);
    }

    @Test
    @DisplayName("Should export tailored resume as DOCX binary stream with Content-Disposition header")
    void testExportDocx() {
        ResponseEntity<byte[]> response = exportController.exportDocx(seededTailoredResume.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment; filename=\"Tailored_Resume_Stripe.docx\""));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 500);
    }
}
