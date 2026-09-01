package com.smartresume.tailor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailoredResumeDto {
    private String candidateName;
    private String email;
    private String phone;
    private String location;
    private String tailoredHeadline;
    private String tailoredSummary;
    private Map<String, List<String>> skillsSection;
    @Builder.Default
    private List<ParsedResumeDto.ExperienceItem> workExperience = new ArrayList<>();
    @Builder.Default
    private List<ParsedResumeDto.EducationItem> education = new ArrayList<>();
    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    private AtsMetrics atsOptimizationMetrics;
    @Builder.Default
    private List<DiffItemDto> diffItems = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtsMetrics {
        private int projectedAtsScore; // 0 - 100
        @Builder.Default
        private List<String> keywordsInjected = new ArrayList<>();
        private int bulletPointsModifiedCount;
    }
}
