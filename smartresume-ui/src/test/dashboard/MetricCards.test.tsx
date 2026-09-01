import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { MetricCards } from '../../components/dashboard/MetricCards';
import { PipelineStats } from '../../types';

describe('MetricCards Component', () => {
  const mockStats: PipelineStats = {
    totalMatches: 24,
    qualifiedCount: 8,
    droppedCount: 16,
    qualificationThreshold: 80,
  };

  it('should render all metric card titles and counts accurately', () => {
    render(<MetricCards stats={mockStats} tailoredCount={5} />);

    expect(screen.getByText('Jobs Ingested')).toBeInTheDocument();
    expect(screen.getByText('24')).toBeInTheDocument();

    expect(screen.getByText('Qualified (≥ 80%)')).toBeInTheDocument();
    expect(screen.getByText('8')).toBeInTheDocument();

    expect(screen.getByText('Dropped (< 80%)')).toBeInTheDocument();
    expect(screen.getByText('16')).toBeInTheDocument();

    expect(screen.getByText('Tailored Resumes')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });
});
