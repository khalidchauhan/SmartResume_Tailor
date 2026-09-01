package com.smartresume.tailor.matcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresume.tailor.domain.entity.BaseResume;
import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.entity.JobPosting;
import com.smartresume.tailor.domain.enums.MatchStatus;
import com.smartresume.tailor.domain.model.MatchEvaluationDto;
import com.smartresume.tailor.domain.model.ParsedJobDto;
import com.smartresume.tailor.domain.model.ParsedResumeDto;
import com.smartresume.tailor.repository.JobMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticMatcherService {

    private static final int QUALIFICATION_THRESHOLD = 80;

    private final JobMatchRepository jobMatchRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.matching.qualification-threshold:80}")
    private int qualificationThreshold = QUALIFICATION_THRESHOLD;

    @Transactional
    public JobMatch evaluateAndScore(BaseResume resume, JobPosting job) {
        log.info("Evaluating match between Resume ID: {} and Job: '{}' ({})", 
                resume.getId(), job.getTitle(), job.getCompany());

        // Check if evaluation already exists
        return jobMatchRepository.findByResumeIdAndJobId(resume.getId(), job.getId())
                .orElseGet(() -> computeAndSaveEvaluation(resume, job));
    }

    private JobMatch computeAndSaveEvaluation(BaseResume resume, JobPosting job) {
        try {
            ParsedResumeDto resumeDto = objectMapper.readValue(resume.getParsedJson(), ParsedResumeDto.class);
            ParsedJobDto jobDto = job.getParsedRequirements() != null 
                    ? objectMapper.readValue(job.getParsedRequirements(), ParsedJobDto.class)
                    : fallbackParseJob(job);

            MatchEvaluationDto evaluation = computeDeterministicScore(resumeDto, jobDto);

            MatchStatus status = evaluation.getOverallScore() >= qualificationThreshold
                    ? MatchStatus.QUALIFIED
                    : MatchStatus.DROPPED_LOW_MATCH;

            String archiveReason = null;
            if (status == MatchStatus.DROPPED_LOW_MATCH) {
                archiveReason = String.format("Overall score (%d%%) is below the required %d%% threshold. Critical gaps: %s",
                        evaluation.getOverallScore(), qualificationThreshold, String.join(", ", evaluation.getCriticalMissingSkills()));
            }

            JobMatch match = JobMatch.builder()
                    .resume(resume)
                    .job(job)
                    .overallScore(evaluation.getOverallScore())
                    .skillsScore(evaluation.getCategoryBreakdown().getSkillsMatchScore())
                    .experienceScore(evaluation.getCategoryBreakdown().getExperienceMatchScore())
                    .domainScore(evaluation.getCategoryBreakdown().getDomainMatchScore())
                    .educationScore(evaluation.getCategoryBreakdown().getEducationCertScore())
                    .status(status)
                    .archiveReason(archiveReason)
                    .evaluationJson(objectMapper.writeValueAsString(evaluation))
                    .build();

            log.info("Evaluation complete for '{}' at {}. Score: {}% -> Status: {}",
                    job.getTitle(), job.getCompany(), evaluation.getOverallScore(), status);

            return jobMatchRepository.save(match);
        } catch (Exception e) {
            log.error("Failed to compute match evaluation: {}", e.getMessage(), e);
            throw new RuntimeException("Evaluation failure: " + e.getMessage(), e);
        }
    }

    public MatchEvaluationDto computeDeterministicScore(ParsedResumeDto resume, ParsedJobDto job) {
        // 1. Skills Scoring (Max 35 points)
        List<String> resumeSkills = resume.getSkills().stream().map(String::toLowerCase).toList();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String reqSkill : job.getRequiredSkills()) {
            if (resumeSkills.stream().anyMatch(s -> s.equalsIgnoreCase(reqSkill) || s.contains(reqSkill.toLowerCase()))) {
                matched.add(reqSkill);
            } else {
                missing.add(reqSkill);
            }
        }

        double skillRatio = job.getRequiredSkills().isEmpty() ? 1.0 : (double) matched.size() / job.getRequiredSkills().size();
        int skillsScore = (int) Math.round(skillRatio * 35.0);

        // 2. Experience & Seniority Scoring (Max 30 points)
        double candidateYears = resume.getYearsOfExperience() != null ? resume.getYearsOfExperience() : 5.0;
        double reqYears = job.getMinYearsExperience() != null ? job.getMinYearsExperience() : 5.0;
        
        int expScore;
        if (candidateYears >= reqYears) {
            expScore = 28 + (candidateYears >= reqYears + 2 ? 2 : 0);
        } else if (candidateYears >= reqYears * 0.8) {
            expScore = 22;
        } else {
            expScore = (int) Math.round((candidateYears / reqYears) * 20.0);
        }
        expScore = Math.min(30, Math.max(0, expScore));

        // 3. Domain & Architecture Alignment (Max 20 points)
        int domainScore = 12; // default baseline
        if (job.getDomain() != null) {
            String domainLower = job.getDomain().toLowerCase();
            String summaryLower = (resume.getSummary() != null ? resume.getSummary() : "").toLowerCase();
            String headlineLower = (resume.getHeadline() != null ? resume.getHeadline() : "").toLowerCase();

            if (domainLower.contains("fintech") || domainLower.contains("cloud") || domainLower.contains("infrastructure") || domainLower.contains("data")) {
                if (summaryLower.contains("distributed") || summaryLower.contains("cloud") || headlineLower.contains("distributed")) {
                    domainScore = 18;
                }
            } else if (domainLower.contains("frontend") && (headlineLower.contains("backend") || headlineLower.contains("systems"))) {
                domainScore = 6; // low domain match for frontend roles when backend focused
            }
        }

        // 4. Education & Baseline (Max 15 points)
        int eduScore = 14; // Default strong CS background

        int overall = Math.min(100, Math.max(0, skillsScore + expScore + domainScore + eduScore));

        String verdict = overall >= qualificationThreshold ? "QUALIFIED_FOR_REFINEMENT" : "DROPPED_LOW_MATCH";
        String reason = overall >= qualificationThreshold
                ? String.format("Candidate qualifies with %d%% alignment. Solid mastery of core tech stack (%s).", 
                        overall, String.join(", ", matched))
                : String.format("Candidate falls below %d%% threshold with %d%% score. Missing critical requirements: %s.",
                        qualificationThreshold, overall, String.join(", ", missing));

        List<String> recommendations = new ArrayList<>();
        if (!missing.isEmpty()) {
            recommendations.add("Incorporate verified experience related to: " + String.join(", ", missing));
        }
        recommendations.add("Quantify throughput and architecture outcomes using Google XYZ formula.");

        return MatchEvaluationDto.builder()
                .overallScore(overall)
                .verdict(verdict)
                .verdictReason(reason)
                .matchedSkills(matched)
                .criticalMissingSkills(missing)
                .seniorityGap(candidateYears >= reqYears ? "None - Seniority requirement met" : "Requires additional years of experience")
                .refinementRecommendations(recommendations)
                .categoryBreakdown(MatchEvaluationDto.CategoryBreakdown.builder()
                        .skillsMatchScore(skillsScore)
                        .experienceMatchScore(expScore)
                        .domainMatchScore(domainScore)
                        .educationCertScore(eduScore)
                        .build())
                .build();
    }

    private ParsedJobDto fallbackParseJob(JobPosting job) {
        return ParsedJobDto.builder()
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .isRemote(job.getIsRemote())
                .minYearsExperience(5.0)
                .requiredSkills(List.of("Java", "Spring Boot", "PostgreSQL", "AWS"))
                .domain("Backend Services")
                .build();
    }
}
