package com.smartresume.tailor.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.domain.model.TailoredResumeDto;
import com.smartresume.tailor.export.service.DocumentExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExportServiceTest {

    @Test
    @DisplayName("Should generate valid ATS PDF and DOCX binary byte arrays")
    void testExportPdfAndDocx() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentExportService exportService = new DocumentExportService(objectMapper);

        TailoredResumeDto dto = TailoredResumeDto.builder()
                .candidateName("Alex Mercer")
                .email("alex@example.com")
                .phone("555-123-4567")
                .location("San Francisco, CA")
                .tailoredHeadline("Staff Distributed Systems Engineer")
                .tailoredSummary("Experienced engineer scaling distributed systems to 120k RPM.")
                .skillsSection(Map.of("Languages", List.of("Java 21", "SQL"), "Frameworks", List.of("Spring Boot", "Kafka")))
                .workExperience(List.of(
                        ParsedResumeDto.ExperienceItem.builder()
                                .company("Tech Corp")
                                .role("Senior Software Engineer")
                                .startDate("2021-03")
                                .endDate("Present")
                                .bullets(List.of("Architected event-driven microservices processing 120k RPM."))
                                .build()
                ))
                .education(List.of(
                        ParsedResumeDto.EducationItem.builder()
                                .degree("B.S. in Computer Science")
                                .institution("UT Austin")
                                .graduationYear("2018")
                                .build()
                ))
                .build();

        JobPosting job = JobPosting.builder()
                .company("Stripe")
                .title("Lead Java Engineer")
                .build();

        JobMatch match = JobMatch.builder()
                .job(job)
                .build();

        TailoredResume tailored = TailoredResume.builder()
                .id(UUID.randomUUID())
                .jobMatch(match)
                .tailoredJson(objectMapper.writeValueAsString(dto))
                .build();

        byte[] pdfBytes = exportService.exportToPdf(tailored);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF should contain valid binary stream");

        byte[] docxBytes = exportService.exportToDocx(tailored);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 500, "DOCX should contain valid binary stream");
    }
}
