package com.smartresume.tailor.regression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.JobIngestRequest;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.domain.model.TailoredResumeDto;
import com.smartresume.tailor.export.service.DocumentExportService;
import com.smartresume.tailor.ingestion.service.JobIngestionService;
import com.smartresume.tailor.ingestion.service.ResumeParserService;
import com.smartresume.tailor.matcher.service.SemanticMatcherService;
import com.smartresume.tailor.refinement.service.ResumeRefinementService;
import com.smartresume.tailor.repository.InMemoryBaseResumeRepository;
import com.smartresume.tailor.repository.InMemoryJobMatchRepository;
import com.smartresume.tailor.repository.InMemoryJobPostingRepository;
import com.smartresume.tailor.repository.InMemoryTailoredResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REGRESSION TEST SUITE: SMARTRESUME TAILOR END-TO-END FUNCTIONAL LIFECYCLE
 */
class SmartResumeRegressionE2ETest {

    private ResumeParserService parserService;
    private JobIngestionService ingestionService;
    private SemanticMatcherService matcherService;
    private ResumeRefinementService refinementService;
    private DocumentExportService exportService;

    private InMemoryBaseResumeRepository resumeRepository;
    private InMemoryJobPostingRepository jobRepository;
    private InMemoryJobMatchRepository matchRepository;
    private InMemoryTailoredResumeRepository tailoredRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        resumeRepository = new InMemoryBaseResumeRepository();
        jobRepository = new InMemoryJobPostingRepository();
        matchRepository = new InMemoryJobMatchRepository();
        tailoredRepository = new InMemoryTailoredResumeRepository();

        parserService = new ResumeParserService(objectMapper);
        ingestionService = new JobIngestionService(jobRepository, objectMapper);
        matcherService = new SemanticMatcherService(matchRepository, objectMapper);
        refinementService = new ResumeRefinementService(tailoredRepository, objectMapper);
        exportService = new DocumentExportService(objectMapper);
    }

    @Test
    @DisplayName("REGRESSION: Full lifecycle from Resume Ingestion -> Matching -> 80% Gate -> Tailoring -> PDF/DOCX Export")
    void testCompleteSmartResumeEndToEndLifecycle() throws Exception {
        // STEP 1: Candidate uploads base resume
        String resumeRawText = """
                Alex Mercer
                alex.mercer@example.com | (555) 234-5678 | San Francisco, CA
                Senior Backend & Distributed Systems Engineer
                
                Summary:
                Experienced software engineer with 8+ years building high-scale distributed backend systems,
                event-driven architectures, and cloud services in Java and Spring Boot.
                
                Skills:
                Java, Java 21, Spring Boot, Apache Kafka, PostgreSQL, Docker, Kubernetes, AWS, Microservices, Redis
                
                Experience:
                Tech Corp — Senior Software Engineer (2021 - Present)
                - Architected event-driven microservices processing 120k RPM with 99.99% availability.
                - Reduced p99 query latency by 42% through PostgreSQL query plan tuning and Redis caching.
                - Mentored 4 junior engineers and led migration of core services to Kubernetes on AWS.
                
                Education:
                B.S. in Computer Science — University of Texas at Austin (2018)
                """;

        ParsedResumeDto parsedResume = parserService.parseFromRawText(resumeRawText, "Alex_Mercer_Senior_SWE.pdf");
        assertNotNull(parsedResume);
        assertEquals("Alex Mercer", parsedResume.getCandidateName());
        assertTrue(parsedResume.getSkills().contains("Java"));
        assertTrue(parsedResume.getSkills().contains("Spring Boot"));

        BaseResume savedResume = resumeRepository.save(BaseResume.builder()
                .fileName("Alex_Mercer_Senior_SWE.pdf")
                .rawText(resumeRawText)
                .parsedJson(objectMapper.writeValueAsString(parsedResume))
                .build());

        // STEP 2: Ingest jobs from catalog
        List<JobPosting> jobs = ingestionService.ingestJobs(JobIngestRequest.builder()
                .keywords(List.of("Java", "Distributed Systems"))
                .locations(List.of("San Francisco", "Remote"))
                .build());

        assertFalse(jobs.isEmpty());
        JobPosting stripeJob = jobs.stream()
                .filter(j -> "Stripe".equalsIgnoreCase(j.getCompany()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Stripe job must exist in catalog"));

        JobPosting vercelJob = jobs.stream()
                .filter(j -> "Vercel".equalsIgnoreCase(j.getCompany()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Vercel job must exist in catalog"));

        // STEP 3: Semantic Match Engine & 80% Threshold
        JobMatch stripeMatch = matcherService.evaluateAndScore(savedResume, stripeJob);
        assertNotNull(stripeMatch);
        assertTrue(stripeMatch.getOverallScore() >= 80);
        assertEquals(MatchStatus.QUALIFIED, stripeMatch.getStatus());
        assertNull(stripeMatch.getArchiveReason());

        JobMatch vercelMatch = matcherService.evaluateAndScore(savedResume, vercelJob);
        assertNotNull(vercelMatch);
        assertTrue(vercelMatch.getOverallScore() < 80);
        assertEquals(MatchStatus.DROPPED_LOW_MATCH, vercelMatch.getStatus());
        assertNotNull(vercelMatch.getArchiveReason());
        assertTrue(vercelMatch.getArchiveReason().contains("below the required 80% threshold"));

        // STEP 4: Refinement Guardrails & Diff
        IllegalStateException gateEx = assertThrows(IllegalStateException.class, () -> {
            refinementService.refineResume(vercelMatch);
        });
        assertTrue(gateEx.getMessage().contains("below the required 80% threshold"));

        TailoredResume tailoredStripe = refinementService.refineResume(stripeMatch);
        assertNotNull(tailoredStripe);
        assertTrue(tailoredStripe.getAtsScore() >= 90);

        TailoredResumeDto tailoredDto = objectMapper.readValue(tailoredStripe.getTailoredJson(), TailoredResumeDto.class);
        assertNotNull(tailoredDto.getTailoredHeadline());
        assertTrue(tailoredDto.getTailoredSummary().contains("Distributed Systems Engineer"));
        assertFalse(tailoredDto.getDiffItems().isEmpty());
        assertTrue(tailoredDto.getDiffItems().stream().anyMatch(d -> d.getChangeType().equals("REPHRASED_XYZ")));
        assertTrue(tailoredDto.getDiffItems().stream().anyMatch(d -> d.getInjectedKeywords().contains("Java 21")));

        // STEP 5: Document Exporter
        byte[] pdfBytes = exportService.exportToPdf(tailoredStripe);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        byte[] docxBytes = exportService.exportToDocx(tailoredStripe);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 500);
    }
}
