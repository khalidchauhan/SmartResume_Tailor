import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { MatchBreakdownModal } from '../../components/matching/MatchBreakdownModal';
import { JobMatch, MatchEvaluationDto } from '../../types';

describe('MatchBreakdownModal Component', () => {
  const mockEvaluation: MatchEvaluationDto = {
    overallScore: 86,
    verdict: 'QUALIFIED_FOR_REFINEMENT',
    verdictReason: 'Candidate exceeds senior requirements with strong distributed systems skills.',
    categoryBreakdown: {
      skillsMatchScore: 31,
      experienceMatchScore: 28,
      domainMatchScore: 15,
      educationCertScore: 12,
    },
    matchedSkills: ['Java 21', 'Spring Boot', 'Kafka', 'AWS'],
    criticalMissingSkills: ['gRPC'],
  };

  const qualifiedMatch: JobMatch = {
    id: 'match-qualified',
    resume: { id: 'r1', fileName: 'Alex.pdf', parsedJson: '{}', createdAt: '' },
    job: {
      id: 'j1',
      externalId: 'ext-1',
      source: 'TEST',
      title: 'Staff Backend Architect',
      company: 'Stripe',
      location: 'San Francisco, CA',
      isRemote: true,
      rawDescription: '',
    },
    overallScore: 86,
    skillsScore: 31,
    experienceScore: 28,
    domainScore: 15,
    educationScore: 12,
    status: 'QUALIFIED',
    evaluationJson: JSON.stringify(mockEvaluation),
    createdAt: '',
  };

  it('should render score breakdown, matched skills, and proceed button for qualified matches', () => {
    const handleProceed = vi.fn();
    const handleClose = vi.fn();

    render(
      <MatchBreakdownModal
        match={qualifiedMatch}
        onClose={handleClose}
        onProceedToTailor={handleProceed}
      />
    );

    expect(screen.getByText('Staff Backend Architect')).toBeInTheDocument();
    expect(screen.getByText('QUALIFIED (≥ 80% THRESHOLD MET)')).toBeInTheDocument();
    expect(screen.getByText('31 / 35 pts')).toBeInTheDocument();
    expect(screen.getByText('28 / 30 pts')).toBeInTheDocument();
    expect(screen.getByText('Java 21')).toBeInTheDocument();
    expect(screen.getByText('gRPC')).toBeInTheDocument();

    const proceedBtn = screen.getByText('Proceed to Resume Refinement');
    expect(proceedBtn).toBeInTheDocument();

    fireEvent.click(proceedBtn);
    expect(handleProceed).toHaveBeenCalledTimes(1);
    expect(handleProceed).toHaveBeenCalledWith('match-qualified');
  });

  it('should not show proceed button for dropped match (< 80%)', () => {
    const droppedMatch: JobMatch = {
      ...qualifiedMatch,
      id: 'match-dropped',
      overallScore: 62,
      status: 'DROPPED_LOW_MATCH',
      archiveReason: 'Below 80% threshold',
    };

    render(
      <MatchBreakdownModal
        match={droppedMatch}
        onClose={vi.fn()}
        onProceedToTailor={vi.fn()}
      />
    );

    expect(screen.getByText('DROPPED / LOW MATCH (< 80%)')).toBeInTheDocument();
    expect(screen.queryByText('Proceed to Resume Refinement')).not.toBeInTheDocument();
  });
});
