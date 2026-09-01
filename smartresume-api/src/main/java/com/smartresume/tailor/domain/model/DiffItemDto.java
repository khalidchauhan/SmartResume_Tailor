package com.smartresume.tailor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffItemDto {
    private String id;
    private String section; // SUMMARY, WORK_EXPERIENCE, SKILLS
    private String context; // e.g. "Tech Corp - Senior Software Engineer"
    private String originalText;
    private String tailoredText;
    private String changeType; // KEYWORD_ENRICHMENT, METRIC_SHARPENING, REPHRASED_XYZ
    @Builder.Default
    private List<String> injectedKeywords = new ArrayList<>();
    private String rationale;
    @Builder.Default
    private boolean accepted = true;
}
