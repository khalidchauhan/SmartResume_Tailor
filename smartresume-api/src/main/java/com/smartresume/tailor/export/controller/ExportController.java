package com.smartresume.tailor.export.controller;

import com.smartresume.tailor.domain.entity.TailoredResume;
import com.smartresume.tailor.export.service.DocumentExportService;
import com.smartresume.tailor.repository.TailoredResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final DocumentExportService documentExportService;
    private final TailoredResumeRepository tailoredResumeRepository;

    @GetMapping("/{tailoredResumeId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID tailoredResumeId) {
        TailoredResume tailored = tailoredResumeRepository.findById(tailoredResumeId)
                .orElseThrow(() -> new IllegalArgumentException("Tailored resume not found: " + tailoredResumeId));

        byte[] pdfBytes = documentExportService.exportToPdf(tailored);

        String filename = "Tailored_Resume_" + tailored.getJobMatch().getJob().getCompany().replaceAll("\\s+", "_") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{tailoredResumeId}/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable UUID tailoredResumeId) {
        TailoredResume tailored = tailoredResumeRepository.findById(tailoredResumeId)
                .orElseThrow(() -> new IllegalArgumentException("Tailored resume not found: " + tailoredResumeId));

        byte[] docxBytes = documentExportService.exportToDocx(tailored);

        String filename = "Tailored_Resume_" + tailored.getJobMatch().getJob().getCompany().replaceAll("\\s+", "_") + ".docx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }
}
