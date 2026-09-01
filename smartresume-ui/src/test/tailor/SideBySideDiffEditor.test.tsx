import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { SideBySideDiffEditor } from '../../components/tailor/SideBySideDiffEditor';
import { TailoredResume, TailoredResumeDto, DiffItemDto } from '../../types';

describe('SideBySideDiffEditor Component', () => {
  const mockDiff: DiffItemDto[] = [
    {
      id: 'diff-1',
      section: 'WORK_EXPERIENCE',
      context: 'Tech Corp - Senior Software Engineer',
      originalText: 'Architected event-driven microservices processing 120k RPM.',
      tailoredText: 'Architected distributed event-driven microservices using Java 21 and Spring Boot, sustaining 120k RPM at 99.99% uptime.',
      changeType: 'REPHRASED_XYZ',
      injectedKeywords: ['Java 21', 'Spring Boot'],
      rationale: 'Applies XYZ formula',
      accepted: true,
    },
  ];

  const mockResumeDto: TailoredResumeDto = {
    candidateName: 'Alex Mercer',
    email: 'alex@example.com',
    phone: '555-123-4567',
    location: 'San Francisco, CA',
    tailoredHeadline: 'Staff Distributed Systems Engineer | High-Throughput Java 21',
    tailoredSummary: 'Staff Distributed Systems Engineer with 8+ years architecting microservices.',
    skillsSection: { Languages: ['Java 21', 'SQL'] },
    workExperience: [],
    education: [],
    atsOptimizationMetrics: {
      projectedAtsScore: 96,
      keywordsInjected: ['Java 21'],
      bulletPointsModifiedCount: 1,
    },
    diffItems: mockDiff,
  };

  const mockTailoredResume: TailoredResume = {
    id: 'tailored-1',
    jobMatch: {
      id: 'match-1',
      resume: { id: 'r1', fileName: 'Alex.pdf', parsedJson: '{}', createdAt: '' },
      job: {
        id: 'j1',
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
    tailoredJson: JSON.stringify(mockResumeDto),
    diffJson: JSON.stringify(mockDiff),
    atsScore: 96,
    status: 'COMPLETED',
  };

  it('should render both original baseline and tailored resume panels', () => {
    render(<SideBySideDiffEditor tailoredResume={mockTailoredResume} onBack={vi.fn()} />);

    expect(screen.getByText('Original Base Resume')).toBeInTheDocument();
    expect(screen.getByText('Tailored ATS-Optimized Resume')).toBeInTheDocument();
    expect(screen.getByText('96%')).toBeInTheDocument();
    expect(screen.getByText(/Architected distributed event-driven microservices/)).toBeInTheDocument();
  });

  it('should toggle bullet point between accepted and reverted states on button click', () => {
    render(<SideBySideDiffEditor tailoredResume={mockTailoredResume} onBack={vi.fn()} />);

    const toggleButton = screen.getByTitle('Revert to original');
    expect(toggleButton).toBeInTheDocument();

    fireEvent.click(toggleButton);

    // After click, button title should change to 'Accept tailored rewrite'
    expect(screen.getByTitle('Accept tailored rewrite')).toBeInTheDocument();
  });

  it('should trigger download links when clicking Export buttons', () => {
    const originalOpen = window.open;
    window.open = vi.fn();

    render(<SideBySideDiffEditor tailoredResume={mockTailoredResume} onBack={vi.fn()} />);

    const exportPdfBtn = screen.getByText('Export Clean PDF');
    fireEvent.click(exportPdfBtn);
    expect(window.open).toHaveBeenCalledWith('/api/v1/export/tailored-1/pdf', '_blank');

    const exportDocxBtn = screen.getByText('Export Word (DOCX)');
    fireEvent.click(exportDocxBtn);
    expect(window.open).toHaveBeenCalledWith('/api/v1/export/tailored-1/docx', '_blank');

    window.open = originalOpen;
  });
});
