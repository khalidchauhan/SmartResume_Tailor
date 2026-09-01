package com.smartresume.tailor.ingestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.model.ApiResponse;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.ingestion.service.ResumeParserService;
import com.smartresume.tailor.repository.BaseResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final BaseResumeRepository baseResumeRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<BaseResume>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) UUID userId) {
        try {
            log.info("Receiving resume upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            ParsedResumeDto parsedDto = resumeParserService.parseResume(file);
            String rawText = file.getOriginalFilename(); // Default fallback

            BaseResume resume = BaseResume.builder()
                    .userId(userId != null ? userId : UUID.randomUUID())
                    .fileName(file.getOriginalFilename())
                    .storageUrl("local://" + file.getOriginalFilename())
                    .rawText(parsedDto.getSummary() != null ? parsedDto.getSummary() : "Extracted Resume Text")
                    .parsedJson(objectMapper.writeValueAsString(parsedDto))
                    .isPrimary(true)
                    .build();

            BaseResume saved = baseResumeRepository.save(resume);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved, "Resume parsed and ingested successfully"));
        } catch (Exception e) {
            log.error("Upload error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to parse resume: " + e.getMessage()));
        }
    }

    @PostMapping("/sample")
    public ResponseEntity<ApiResponse<BaseResume>> loadSampleResume() {
        try {
            String sampleText = """
                Alex Mercer
                alex.mercer@example.com | (555) 234-5678 | San Francisco, CA
                Senior Backend & Distributed Systems Engineer
                
                Professional Summary:
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

            ParsedResumeDto parsed = resumeParserService.parseFromRawText(sampleText, "Alex_Mercer_Senior_SWE.pdf");

            BaseResume resume = BaseResume.builder()
                    .userId(UUID.randomUUID())
                    .fileName("Alex_Mercer_Senior_SWE.pdf")
                    .storageUrl("local://Alex_Mercer_Senior_SWE.pdf")
                    .rawText(sampleText)
                    .parsedJson(objectMapper.writeValueAsString(parsed))
                    .isPrimary(true)
                    .build();

            BaseResume saved = baseResumeRepository.save(resume);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved, "Default sample resume loaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<BaseResume>> getLatestResume() {
        return baseResumeRepository.findFirstByOrderByCreatedAtDesc()
                .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("No resumes found")));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BaseResume>>> getAllResumes() {
        return ResponseEntity.ok(ApiResponse.ok(baseResumeRepository.findAll()));
    }
}
