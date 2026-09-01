import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { JobPipelineTable } from '../../components/dashboard/JobPipelineTable';
import { JobMatch } from '../../types';

describe('JobPipelineTable Component', () => {
  const mockMatches: JobMatch[] = [
    {
      id: 'match-1',
      resume: { id: 'res-1', fileName: 'Alex.pdf', parsedJson: '{}', createdAt: '' },
      job: {
        id: 'job-1',
        externalId: 'ext-1',
        source: 'TEST',
        title: 'Lead Java Systems Engineer',
        company: 'Stripe',
        location: 'San Francisco, CA',
        isRemote: true,
        rawDescription: '',
      },
      overallScore: 88,
      skillsScore: 32,
      experienceScore: 28,
      domainScore: 16,
      educationScore: 12,
      status: 'QUALIFIED',
      evaluationJson: '{}',
      createdAt: '',
    },
    {
      id: 'match-2',
      resume: { id: 'res-1', fileName: 'Alex.pdf', parsedJson: '{}', createdAt: '' },
      job: {
        id: 'job-2',
        externalId: 'ext-2',
        source: 'TEST',
        title: 'Senior Frontend Dev',
        company: 'Vercel',
        location: 'Remote',
        isRemote: true,
        rawDescription: '',
      },
      overallScore: 58,
      skillsScore: 12,
      experienceScore: 20,
      domainScore: 12,
      educationScore: 14,
      status: 'DROPPED_LOW_MATCH',
      archiveReason: 'Missing core frontend stack (React/Next.js)',
      evaluationJson: '{}',
      createdAt: '',
    },
  ];

  it('should render all job rows with scores and threshold badges', () => {
    render(
      <JobPipelineTable
        matches={mockMatches}
        onViewBreakdown={vi.fn()}
        onTailor={vi.fn()}
        onViewDiff={vi.fn()}
        tailoredMatchIds={new Set()}
        isLoading={false}
      />
    );

    expect(screen.getByText('Lead Java Systems Engineer')).toBeInTheDocument();
    expect(screen.getByText('Stripe')).toBeInTheDocument();
    expect(screen.getByText('QUALIFIED (≥ 80%)')).toBeInTheDocument();

    expect(screen.getByText('Senior Frontend Dev')).toBeInTheDocument();
    expect(screen.getByText('Vercel')).toBeInTheDocument();
    expect(screen.getByText('DROPPED (< 80%)')).toBeInTheDocument();
  });

  it('should filter jobs when clicking filter tabs', () => {
    render(
      <JobPipelineTable
        matches={mockMatches}
        onViewBreakdown={vi.fn()}
        onTailor={vi.fn()}
        onViewDiff={vi.fn()}
        tailoredMatchIds={new Set()}
        isLoading={false}
      />
    );

    // Click Qualified filter
    const qualifiedFilterBtn = screen.getByText(/Qualified ≥80%/);
    fireEvent.click(qualifiedFilterBtn);

    expect(screen.getByText('Lead Java Systems Engineer')).toBeInTheDocument();
    expect(screen.queryByText('Senior Frontend Dev')).not.toBeInTheDocument();

    // Click Dropped filter
    const droppedFilterBtn = screen.getByText(/Dropped <80%/);
    fireEvent.click(droppedFilterBtn);

    expect(screen.queryByText('Lead Java Systems Engineer')).not.toBeInTheDocument();
    expect(screen.getByText('Senior Frontend Dev')).toBeInTheDocument();
  });

  it('should trigger breakdown view when clicking Breakdown button', () => {
    const handleViewBreakdown = vi.fn();

    render(
      <JobPipelineTable
        matches={mockMatches}
        onViewBreakdown={handleViewBreakdown}
        onTailor={vi.fn()}
        onViewDiff={vi.fn()}
        tailoredMatchIds={new Set()}
        isLoading={false}
      />
    );

    const breakdownButtons = screen.getAllByText('Breakdown');
    fireEvent.click(breakdownButtons[0]);

    expect(handleViewBreakdown).toHaveBeenCalledTimes(1);
    expect(handleViewBreakdown).toHaveBeenCalledWith(mockMatches[0]);
  });
});
