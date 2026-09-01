import { test, expect } from '@playwright/test';

test.describe('SmartResume Tailor - Visual Browser Automation', () => {
  test.beforeEach(async ({ page }) => {
    // Intercept backend API requests so the browser automation works standalone
    await page.route('**/api/v1/resumes/latest', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 'resume-sample-id',
            fileName: 'Alex_Mercer_Senior_SWE.pdf',
            parsedJson: '{}',
            createdAt: new Date().toISOString(),
          },
        }),
      });
    });

    await page.route('**/api/v1/resumes/sample', async (route) => {
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 'resume-sample-id',
            fileName: 'Alex_Mercer_Senior_SWE.pdf',
            parsedJson: '{}',
            createdAt: new Date().toISOString(),
          },
        }),
      });
    });

    await page.route('**/api/v1/jobs/ingest', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'j1', company: 'Stripe', title: 'Lead Java Systems Engineer' },
            { id: 'j2', company: 'Vercel', title: 'Senior Frontend Dev' },
          ],
        }),
      });
    });

    await page.route('**/api/v1/matches/stats', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            totalMatches: 2,
            qualifiedCount: 1,
            droppedCount: 1,
            qualificationThreshold: 80,
          },
        }),
      });
    });

    await page.route('**/api/v1/matches/evaluate-all', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 'match-stripe-01',
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
                verdictReason: 'Candidate exceeds senior requirements with strong distributed systems skills.',
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
              id: 'match-vercel-01',
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
              archiveReason: 'Candidate lacks React, Next.js, and TypeScript requirements.',
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
    });

    await page.route('**/api/v1/tailor/match-stripe-01/generate', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 'tailored-stripe-01',
            jobMatch: {
              id: 'match-stripe-01',
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
    });
  });

  test('Walkthrough: Load UI, Search Jobs, Filter Table, Inspect Modal & Tailor in Live Browser', async ({ page }) => {
    // 1. Navigate to frontend application
    await page.goto('/');

    // Verify Title & Branding
    await expect(page.locator('header').getByText('SmartResume', { exact: true })).toBeVisible();
    await expect(page.getByText('Active Base Resume')).toBeVisible();

    // 2. Automate User Typing: Keywords & Location with human delay
    const keywordsInput = page.getByPlaceholder(/Target Keywords/i);
    await keywordsInput.click();
    await keywordsInput.fill('Kubernetes, Go, Kafka');
    await page.waitForTimeout(400);

    const locationInput = page.getByPlaceholder(/Location/i);
    await locationInput.click();
    await locationInput.fill('Seattle, WA / Remote');
    await page.waitForTimeout(400);

    // 3. Automate Clicking "Ingest & Match Pipeline"
    const ingestBtn = page.getByRole('button', { name: /Ingest & Match Pipeline/i });
    await ingestBtn.click();
    await page.waitForTimeout(600);

    // 4. Assert Job Rows Appear in Table
    await expect(page.getByRole('cell', { name: 'Lead Java Systems Engineer' })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Senior Frontend Dev' })).toBeVisible();

    // 5. Test Filter Tabs in Browser
    const qualifiedTab = page.getByRole('button', { name: /Qualified ≥80%/i });
    await qualifiedTab.click();
    await page.waitForTimeout(500);
    await expect(page.getByRole('cell', { name: 'Lead Java Systems Engineer' })).toBeVisible();
    await expect(page.getByRole('cell', { name: 'Senior Frontend Dev' })).not.toBeVisible();

    const droppedTab = page.getByRole('button', { name: /Dropped <80%/i });
    await droppedTab.click();
    await page.waitForTimeout(500);
    await expect(page.getByRole('cell', { name: 'Lead Java Systems Engineer' })).not.toBeVisible();
    await expect(page.getByRole('cell', { name: 'Senior Frontend Dev' })).toBeVisible();

    const allTab = page.getByRole('button', { name: /All \(/i });
    await allTab.click();
    await page.waitForTimeout(500);

    // 6. Click Breakdown Button to open modal
    const breakdownButtons = page.getByRole('button', { name: /Breakdown/i });
    await breakdownButtons.first().click();
    await page.waitForTimeout(600);

    // Assert modal elements
    await expect(page.getByText('QUALIFIED (≥ 80% THRESHOLD MET)')).toBeVisible();
    await expect(page.getByRole('button', { name: /Proceed to Resume Refinement/i })).toBeVisible();

    // 7. Click "Proceed to Resume Refinement"
    const proceedBtn = page.getByRole('button', { name: /Proceed to Resume Refinement/i });
    await proceedBtn.click();
    await page.waitForTimeout(700);

    // 8. Verify Side-by-Side Diff Studio
    await expect(page.getByText('Side-by-Side ATS Comparison & Diff')).toBeVisible();
    await expect(page.getByText('Tailored ATS-Optimized Resume')).toBeVisible();
    await expect(page.getByText('98%')).toBeVisible();

    // 9. Toggle Diff Rewrite Accept / Revert
    const toggleBtn = page.locator('button[title="Revert to original"]');
    await expect(toggleBtn).toBeVisible();
    await toggleBtn.click();
    await page.waitForTimeout(500);

    // Verify state toggled to accept
    await expect(page.locator('button[title="Accept tailored rewrite"]')).toBeVisible();

    // 10. Click Back to Dashboard
    const backBtn = page.locator('main button').first();
    await backBtn.click();
    await page.waitForTimeout(500);

    // Verify returned to pipeline
    await expect(page.getByText('Active Job Pipeline')).toBeVisible();
  });
});
