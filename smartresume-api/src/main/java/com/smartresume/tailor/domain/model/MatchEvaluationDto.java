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
public class MatchEvaluationDto {
    private int overallScore; // 0 - 100
    private String verdict;   // QUALIFIED_FOR_REFINEMENT vs DROPPED_LOW_MATCH
    private CategoryBreakdown categoryBreakdown;
    private String verdictReason;
    @Builder.Default
    private List<String> matchedSkills = new ArrayList<>();
    @Builder.Default
    private List<String> criticalMissingSkills = new ArrayList<>();
    private String seniorityGap;
    @Builder.Default
    private List<String> refinementRecommendations = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private int skillsMatchScore;      // Max 35
        private int experienceMatchScore;  // Max 30
        private int domainMatchScore;      // Max 20
        private int educationCertScore;    // Max 15
    }
}
