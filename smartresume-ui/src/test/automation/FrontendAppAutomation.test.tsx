import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { App } from '../../App';

describe('AUTOMATION TEST SUITE: End-to-End Frontend Application Automation', () => {
  let user: ReturnType<typeof userEvent.setup>;

  beforeEach(() => {
    user = userEvent.setup();

    // Mock backend REST services
    globalThis.fetch = vi.fn((url: string, options?: any) => {
      const urlStr = url.toString();

      if (urlStr.includes('/resumes/latest')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                id: 'base-resume-01',
                fileName: 'Alex_Mercer_Senior_SWE.pdf',
                parsedJson: JSON.stringify({
                  candidateName: 'Alex Mercer',
                  email: 'alex@example.com',
                  skills: ['Java 21', 'Spring Boot', 'Kafka', 'AWS'],
                }),
                createdAt: new Date().toISOString(),
              },
            }),
        });
      }

      if (urlStr.includes('/resumes/upload')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              success: true,
              data: {
                id: 'uploaded-resume-02',
                fileName: 'Custom_Lead_Dev.pdf',
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
                id: 'sample-resume-01',
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
                { id: 'job-1', company: 'Stripe', title: 'Lead Java Systems Engineer' },
                { id: 'job-2', company: 'Vercel', title: 'Senior Frontend Dev' },
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
                  resume: { id: 'r1', fileName: 'Alex_Mercer_Senior_SWE.pdf' },
                  job: {
                    id: 'j1',
                    company: 'Stripe',
                    title: 'Lead Java Systems Engineer',
                    location: 'San Francisco, CA',
                    isRemote: true,
                  },
                  overallScore: 92,
                  skillsScore: 34,
                  experienceScore: 28,
                  domainScore: 18,
                  educationScore: 12,
                  status: 'QUALIFIED',
                  evaluationJson: JSON.stringify({
                    overallScore: 92,
                    verdict: 'QUALIFIED_FOR_REFINEMENT',
                    verdictReason: 'Exceptional alignment with distributed ledger and microservices requirements.',
                    categoryBreakdown: {
                      skillsMatchScore: 34,
                      experienceMatchScore: 28,
                      domainMatchScore: 18,
                      educationCertScore: 12,
                    },
                    matchedSkills: ['Java 21', 'Spring Boot', 'Kafka', 'PostgreSQL', 'AWS'],
                    criticalMissingSkills: [],
                  }),
                  createdAt: new Date().toISOString(),
                },
                {
                  id: 'match-vercel',
                  resume: { id: 'r1', fileName: 'Alex_Mercer_Senior_SWE.pdf' },
                  job: {
                    id: 'j2',
                    company: 'Vercel',
                    title: 'Senior Frontend Dev',
                    location: 'Remote',
                    isRemote: true,
                  },
                  overallScore: 48,
                  skillsScore: 10,
                  experienceScore: 16,
                  domainScore: 10,
                  educationScore: 12,
                  status: 'DROPPED_LOW_MATCH',
                  archiveReason: 'Candidate lacks React, Next.js, and TypeScript core requirements.',
                  evaluationJson: JSON.stringify({
                    overallScore: 48,
                    verdict: 'DROPPED_LOW_MATCH',
                    verdictReason: 'Severe skill gap for frontend framework roles.',
                    categoryBreakdown: {
                      skillsMatchScore: 10,
                      experienceMatchScore: 16,
                      domainMatchScore: 10,
                      educationCertScore: 12,
                    },
                    matchedSkills: [],
                    criticalMissingSkills: ['React', 'Next.js', 'TypeScript', 'CSS'],
                  }),
                  createdAt: new Date().toISOString(),
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
                id: 'tailored-stripe-01',
                jobMatch: {
                  id: 'match-stripe',
                  job: { company: 'Stripe', title: 'Lead Java Systems Engineer' },
                },
                tailoredJson: JSON.stringify({
                  candidateName: 'Alex Mercer',
                  email: 'alex@example.com',
                  phone: '555-0192',
                  location: 'San Francisco, CA',
                  tailoredHeadline: 'Staff Distributed Systems Engineer | High-Scale Ledger Systems',
                  tailoredSummary: 'Staff Distributed Systems Engineer with 8+ years designing fault-tolerant platforms.',
                  skillsSection: {
                    Languages: ['Java 21', 'SQL', 'Bash'],
                    Frameworks: ['Spring Boot 3', 'Apache Kafka', 'gRPC'],
                  },
                  workExperience: [
                    {
                      company: 'Tech Corp',
                      role: 'Staff Engineer',
                      startDate: '2021-03',
                      endDate: 'Present',
                      bullets: [
                        'Architected distributed event streaming platform handling 150k RPM with 99.999% reliability.',
                      ],
                    },
                  ],
                  atsOptimizationMetrics: {
                    projectedAtsScore: 98,
                    keywordsInjected: ['Java 21', 'Distributed Transactions', 'Spring Boot 3'],
                    bulletPointsModifiedCount: 1,
                  },
                  diffItems: [
                    {
                      id: 'diff-item-1',
                      section: 'WORK_EXPERIENCE',
                      context: 'Tech Corp - Staff Engineer',
                      originalText: 'Built high throughput ledger processing 150k RPM.',
                      tailoredText: 'Architected distributed event streaming platform handling 150k RPM using Java 21 and Kafka with 99.999% reliability.',
                      changeType: 'REPHRASED_XYZ',
                      injectedKeywords: ['Java 21', 'Kafka', '99.999% reliability'],
                      rationale: 'Reformulates achievement according to Google XYZ formula with concrete scale metrics',
                      accepted: true,
                    },
                  ],
                }),
                diffJson: JSON.stringify([
                  {
                    id: 'diff-item-1',
                    section: 'WORK_EXPERIENCE',
                    context: 'Tech Corp - Staff Engineer',
                    originalText: 'Built high throughput ledger processing 150k RPM.',
                    tailoredText: 'Architected distributed event streaming platform handling 150k RPM using Java 21 and Kafka with 99.999% reliability.',
                    changeType: 'REPHRASED_XYZ',
                    injectedKeywords: ['Java 21', 'Kafka', '99.999% reliability'],
                    rationale: 'Reformulates achievement according to Google XYZ formula with concrete scale metrics',
                    accepted: true,
                  },
                ]),
                atsScore: 98,
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

  it('AUTOMATION: Simulates realistic user typing search keywords, uploading resume, filtering pipeline, and diff tailoring', async () => {
    render(<App />);

    // 1. Verify header renders
    await waitFor(() => {
      expect(screen.getByText('SmartResume')).toBeInTheDocument();
      expect(screen.getByText('Active Base Resume')).toBeInTheDocument();
    });

    // 2. Automate User Input: Clear & Type custom search keywords
    await waitFor(() => {
      expect(screen.getByPlaceholderText(/Target Keywords/i)).toBeInTheDocument();
    });
    const keywordsInput = screen.getByPlaceholderText(/Target Keywords/i);
    await user.clear(keywordsInput);
    await user.type(keywordsInput, 'Kubernetes, Go, Kafka');
    expect(keywordsInput).toHaveValue('Kubernetes, Go, Kafka');

    // 3. Automate User Input: Type target location
    const locationInput = screen.getByPlaceholderText(/Location/i);
    await user.clear(locationInput);
    await user.type(locationInput, 'Seattle, WA / Remote');
    expect(locationInput).toHaveValue('Seattle, WA / Remote');

    // 4. Automate File Upload Selection
    const uploadInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(uploadInput).toBeInTheDocument();
    const fakeFile = new File(['Candidate raw resume text'], 'Custom_Lead_Dev.pdf', { type: 'application/pdf' });
    await user.upload(uploadInput, fakeFile);

    // 5. Automate Ingest & Match Pipeline Button Click
    const ingestButton = screen.getByText('Ingest & Match Pipeline');
    await user.click(ingestButton);

    // 6. Verify Table Data & Filtering
    await waitFor(() => {
      expect(screen.getByText('Lead Java Systems Engineer')).toBeInTheDocument();
      expect(screen.getByText('Senior Frontend Dev')).toBeInTheDocument();
    });

    // Automate filter clicking: 'Qualified ≥80%'
    const qualifiedTab = screen.getByText(/Qualified ≥80%/);
    await user.click(qualifiedTab);

    expect(screen.getByText('Lead Java Systems Engineer')).toBeInTheDocument();
    expect(screen.queryByText('Senior Frontend Dev')).not.toBeInTheDocument();

    // Automate filter clicking: 'Dropped <80%'
    const droppedTab = screen.getByText(/Dropped <80%/);
    await user.click(droppedTab);

    expect(screen.queryByText('Lead Java Systems Engineer')).not.toBeInTheDocument();
    expect(screen.getByText('Senior Frontend Dev')).toBeInTheDocument();

    // Return to 'All'
    const allTab = screen.getByText(/All \(/);
    await user.click(allTab);

    // 7. Automate Opening Match Breakdown Modal
    const breakdownButtons = screen.getAllByText('Breakdown');
    await user.click(breakdownButtons[0]);

    await waitFor(() => {
      expect(screen.getByText('QUALIFIED (≥ 80% THRESHOLD MET)')).toBeInTheDocument();
      expect(screen.getByText('34 / 35 pts')).toBeInTheDocument();
      expect(screen.getByText('Proceed to Resume Refinement')).toBeInTheDocument();
    });

    // 8. Automate Proceed to Tailor
    const proceedBtn = screen.getByText('Proceed to Resume Refinement');
    await user.click(proceedBtn);

    // 9. Automate Diff Studio Verification
    await waitFor(() => {
      expect(screen.getByText('Side-by-Side ATS Comparison & Diff')).toBeInTheDocument();
      expect(screen.getByText('98%')).toBeInTheDocument();
      expect(screen.getByText('Export Clean PDF')).toBeInTheDocument();
      expect(screen.getByText('Export Word (DOCX)')).toBeInTheDocument();
    });

    // 10. Automate Diff Rewrite Toggle
    const toggleDiffBtn = screen.getByTitle('Revert to original');
    await user.click(toggleDiffBtn);

    // Assert state toggled
    expect(screen.getByTitle('Accept tailored rewrite')).toBeInTheDocument();

    // 11. Automate Export Actions
    const windowOpenSpy = vi.spyOn(window, 'open').mockImplementation(() => null);

    const exportPdfBtn = screen.getByText('Export Clean PDF');
    await user.click(exportPdfBtn);
    expect(windowOpenSpy).toHaveBeenCalledWith('/api/v1/export/tailored-stripe-01/pdf', '_blank');

    const exportDocxBtn = screen.getByText('Export Word (DOCX)');
    await user.click(exportDocxBtn);
    expect(windowOpenSpy).toHaveBeenCalledWith('/api/v1/export/tailored-stripe-01/docx', '_blank');

    windowOpenSpy.mockRestore();

    // 12. Automate Return to Pipeline
    const backButton = screen.getByRole('button', { name: '' });
    await user.click(backButton);

    await waitFor(() => {
      expect(screen.getByText('Active Job Pipeline')).toBeInTheDocument();
    });
  });
});
