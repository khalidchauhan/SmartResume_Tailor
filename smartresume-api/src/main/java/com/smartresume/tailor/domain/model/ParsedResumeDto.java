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
public class ParsedResumeDto {
    private String candidateName;
    private String email;
    private String phone;
    private String location;
    private String headline;
    private String summary;
    private Double yearsOfExperience;
    @Builder.Default
    private List<String> skills = new ArrayList<>();
    @Builder.Default
    private List<ExperienceItem> experience = new ArrayList<>();
    @Builder.Default
    private List<EducationItem> education = new ArrayList<>();
    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceItem {
        private String company;
        private String role;
        private String startDate;
        private String endDate; // null or "Present"
        private String location;
        @Builder.Default
        private List<String> bullets = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItem {
        private String degree;
        private String institution;
        private String graduationYear;
    }
}
