import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';
import { App } from '../../App';

describe('FRONTEND REGRESSION SUITE: SmartResume Tailor User Workflow', () => {
  beforeEach(() => {
    // Mock window.fetch responses simulating a live Spring Boot backend
    globalThis.fetch = vi.fn((url: string, options?: any) => {
      const urlStr = url.toString();

      if (urlStr.includes('/resumes/latest')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                id: 'resume-1',
                fileName: 'Alex_Mercer_Senior_SWE.pdf',
                parsedJson: '{}',
                createdAt: new Date().toISOString(),
              },
            }),
        });
      }

      if (urlStr.includes('/resumes/sample')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                id: 'resume-1',
                fileName: 'Alex_Mercer_Senior_SWE.pdf',
                parsedJson: '{}',
                createdAt: new Date().toISOString(),
              },
            }),
        });
      }

      if (urlStr.includes('/jobs/ingest')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: [
                { id: 'j1', company: 'Stripe', title: 'Lead Java Systems Engineer' },
                { id: 'j2', company: 'Vercel', title: 'Senior Frontend Dev' },
              ],
            }),
        });
      }

      if (urlStr.includes('/matches/stats')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                totalMatches: 2,
                qualifiedCount: 1,
                droppedCount: 1,
                qualificationThreshold: 80,
              },
            }),
        });
      }

      if (urlStr.includes('/matches/evaluate-all') || urlStr.includes('/matches')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: [
                {
                  id: 'match-stripe',
                  resume: { id: 'r1', fileName: 'Alex.pdf' },
                  job: { id: 'j1', company: 'Stripe', title: 'Lead Java Systems Engineer', location: 'San Francisco', isRemote: true },
                  overallScore: 88,
                  skillsScore: 32,
                  experienceScore: 28,
                  domainScore: 16,
                  educationScore: 12,
                  status: 'QUALIFIED',
                  evaluationJson: JSON.stringify({
                    overallScore: 88,
                    verdict: 'QUALIFIED_FOR_REFINEMENT',
                    verdictReason: 'Strong distributed systems and Java experience.',
                    categoryBreakdown: { skillsMatchScore: 32, experienceMatchScore: 28, domainMatchScore: 16, educationCertScore: 12 },
                    matchedSkills: ['Java 21', 'Spring Boot', 'Kafka'],
                    criticalMissingSkills: [],
                  }),
                  createdAt: '',
                },
                {
                  id: 'match-vercel',
                  resume: { id: 'r1', fileName: 'Alex.pdf' },
                  job: { id: 'j2', company: 'Vercel', title: 'Senior Frontend Dev', location: 'Remote', isRemote: true },
                  overallScore: 56,
                  skillsScore: 12,
                  experienceScore: 18,
                  domainScore: 12,
                  educationScore: 14,
                  status: 'DROPPED_LOW_MATCH',
                  archiveReason: 'Score below 80% threshold',
                  evaluationJson: JSON.stringify({
                    overallScore: 56,
                    verdict: 'DROPPED_LOW_MATCH',
                    verdictReason: 'Missing frontend React/Node stack.',
                    categoryBreakdown: { skillsMatchScore: 12, experienceMatchScore: 18, domainMatchScore: 12, educationCertScore: 14 },
                    matchedSkills: [],
                    criticalMissingSkills: ['React', 'Next.js'],
                  }),
                  createdAt: '',
                },
              ],
            }),
        });
      }

      if (urlStr.includes('/tailor/match-stripe/generate')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                id: 'tailored-stripe',
                jobMatch: {
                  id: 'match-stripe',
                  job: { company: 'Stripe', title: 'Lead Java Systems Engineer' },
                },
                tailoredJson: JSON.stringify({
                  candidateName: 'Alex Mercer',
                  email: 'alex@example.com',
                  phone: '555-123',
                  location: 'San Francisco, CA',
                  tailoredHeadline: 'Staff Distributed Systems Engineer',
                  tailoredSummary: 'Staff Distributed Systems Engineer with 8+ years experience.',
                  skillsSection: { Languages: ['Java 21'] },
                  workExperience: [],
                  atsOptimizationMetrics: { projectedAtsScore: 96, keywordsInjected: ['Java 21'], bulletPointsModifiedCount: 1 },
                  diffItems: [
                    {
                      id: 'diff-1',
                      section: 'WORK_EXPERIENCE',
                      context: 'Tech Corp - Senior Software Engineer',
                      originalText: 'Architected microservices.',
                      tailoredText: 'Architected distributed event-driven microservices using Java 21.',
                      changeType: 'REPHRASED_XYZ',
                      injectedKeywords: ['Java 21'],
                      rationale: 'Google XYZ formula applied',
                      accepted: true,
                    },
                  ],
                }),
                diffJson: JSON.stringify([
                  {
                    id: 'diff-1',
                    section: 'WORK_EXPERIENCE',
                    context: 'Tech Corp - Senior Software Engineer',
                    originalText: 'Architected microservices.',
                    tailoredText: 'Architected distributed event-driven microservices using Java 21.',
                    changeType: 'REPHRASED_XYZ',
                    injectedKeywords: ['Java 21'],
                    rationale: 'Google XYZ formula applied',
                    accepted: true,
                  },
                ]),
                atsScore: 96,
                status: 'COMPLETED',
              },
            }),
        });
      }

      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true, data: {} }),
      });
    }) as any;
  });

  it('should execute full end-to-end user regression walkthrough smoothly', async () => {
    render(<App />);

    // 1. Initial Load: Header, stats, and pipeline render
    await waitFor(() => {
      expect(screen.getByText('SmartResume')).toBeInTheDocument();
      expect(screen.getByText('Lead Java Systems Engineer')).toBeInTheDocument();
      expect(screen.getByText('Senior Frontend Dev')).toBeInTheDocument();
    });

    // 2. Metric cards reflect pipeline numbers
    expect(screen.getByText('Jobs Ingested')).toBeInTheDocument();
    expect(screen.getByText('Qualified (≥ 80%)')).toBeInTheDocument();
    expect(screen.getByText('Dropped (< 80%)')).toBeInTheDocument();

    // 3. User views Match Breakdown for Stripe role
    const breakdownButtons = screen.getAllByText('Breakdown');
    fireEvent.click(breakdownButtons[0]);

    await waitFor(() => {
      expect(screen.getByText('QUALIFIED (≥ 80% THRESHOLD MET)')).toBeInTheDocument();
      expect(screen.getByText('32 / 35 pts')).toBeInTheDocument();
      expect(screen.getByText('Proceed to Resume Refinement')).toBeInTheDocument();
    });

    // 4. User clicks "Proceed to Resume Refinement"
    const proceedBtn = screen.getByText('Proceed to Resume Refinement');
    fireEvent.click(proceedBtn);

    // 5. User enters Side-by-Side Diff Studio
    await waitFor(() => {
      expect(screen.getByText('Side-by-Side ATS Comparison & Diff')).toBeInTheDocument();
      expect(screen.getByText('Original Base Resume')).toBeInTheDocument();
      expect(screen.getByText('Tailored ATS-Optimized Resume')).toBeInTheDocument();
      expect(screen.getByText('96%')).toBeInTheDocument();
      expect(screen.getByText('Export Clean PDF')).toBeInTheDocument();
      expect(screen.getByText('Export Word (DOCX)')).toBeInTheDocument();
    });

    // 6. User toggles diff bullet state
    const toggleButton = screen.getByTitle('Revert to original');
    fireEvent.click(toggleButton);
    expect(screen.getByTitle('Accept tailored rewrite')).toBeInTheDocument();

    // 7. User clicks back button to return to dashboard
    const backBtn = screen.getByRole('button', { name: '' });
    fireEvent.click(backBtn);

    await waitFor(() => {
      expect(screen.getByText('Active Job Pipeline')).toBeInTheDocument();
    });
  });
});
