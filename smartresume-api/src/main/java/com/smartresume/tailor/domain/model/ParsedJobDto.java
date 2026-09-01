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
public class ParsedJobDto {
    private String title;
    private String company;
    private String location;
    private boolean isRemote;
    private String seniorityLevel; // Junior, Mid, Senior, Staff, Lead, Principal
    private Double minYearsExperience;
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();
    @Builder.Default
    private List<String> niceToHaveSkills = new ArrayList<>();
    @Builder.Default
    private List<String> responsibilities = new ArrayList<>();
    private String domain; // Fintech, Cloud, SaaS, Healthcare, etc.
}
