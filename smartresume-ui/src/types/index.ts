export type MatchStatus = 'DROPPED_LOW_MATCH' | 'QUALIFIED' | 'ARCHIVED';
export type TailorStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

export interface BaseResume {
  id: string;
  fileName: string;
  parsedJson: string;
  storageUrl?: string;
  createdAt: string;
}

export interface ParsedResumeDto {
  candidateName: string;
  email: string;
  phone: string;
  location: string;
  headline: string;
  summary: string;
  yearsOfExperience: number;
  skills: string[];
  experience: {
    company: string;
    role: string;
    startDate: string;
    endDate?: string;
    location?: string;
    bullets: string[];
  }[];
  education: {
    degree: string;
    institution: string;
    graduationYear: string;
  }[];
  certifications?: string[];
}

export interface JobPosting {
  id: string;
  externalId: string;
  source: string;
  title: string;
  company: string;
  location: string;
  isRemote: boolean;
  rawDescription: string;
  parsedRequirements?: string;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  postedAt?: string;
}

export interface MatchEvaluationDto {
  overallScore: number;
  verdict: 'QUALIFIED_FOR_REFINEMENT' | 'DROPPED_LOW_MATCH';
  verdictReason: string;
  categoryBreakdown: {
    skillsMatchScore: number;     // Max 35
    experienceMatchScore: number; // Max 30
    domainMatchScore: number;     // Max 20
    educationCertScore: number;   // Max 15
  };
  matchedSkills: string[];
  criticalMissingSkills: string[];
  seniorityGap?: string;
  refinementRecommendations?: string[];
}

export interface JobMatch {
  id: string;
  resume: BaseResume;
  job: JobPosting;
  overallScore: number;
  skillsScore: number;
  experienceScore: number;
  domainScore: number;
  educationScore: number;
  status: MatchStatus;
  archiveReason?: string;
  evaluationJson: string;
  createdAt: string;
}

export interface DiffItemDto {
  id: string;
  section: string;
  context: string;
  originalText: string;
  tailoredText: string;
  changeType: string;
  injectedKeywords: string[];
  rationale: string;
  accepted: boolean;
}

export interface TailoredResumeDto {
  candidateName: string;
  email: string;
  phone: string;
  location: string;
  tailoredHeadline: string;
  tailoredSummary: string;
  skillsSection: Record<string, string[]>;
  workExperience: {
    company: string;
    role: string;
    startDate: string;
    endDate?: string;
    bullets: string[];
  }[];
  education: {
    degree: string;
    institution: string;
    graduationYear: string;
  }[];
  atsOptimizationMetrics: {
    projectedAtsScore: number;
    keywordsInjected: string[];
    bulletPointsModifiedCount: number;
  };
  diffItems: DiffItemDto[];
}

export interface TailoredResume {
  id: string;
  jobMatch: JobMatch;
  tailoredJson: string;
  diffJson: string;
  atsScore: number;
  status: TailorStatus;
  pdfExportUrl?: string;
  docxExportUrl?: string;
}

export interface PipelineStats {
  totalMatches: number;
  qualifiedCount: number;
  droppedCount: number;
  qualificationThreshold: number;
}
