import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { JobSearchControls } from '../../components/dashboard/JobSearchControls';
import { BaseResume } from '../../types';

describe('JobSearchControls Component', () => {
  const mockResume: BaseResume = {
    id: 'resume-123',
    fileName: 'Alex_Mercer_Senior_SWE.pdf',
    parsedJson: '{}',
    createdAt: new Date().toISOString(),
  };

  it('should render active resume name', () => {
    render(
      <JobSearchControls
        currentResume={mockResume}
        onIngestJobs={vi.fn()}
        onLoadSampleResume={vi.fn()}
        onUploadResume={vi.fn()}
        isLoading={false}
      />
    );

    expect(screen.getByText('Alex_Mercer_Senior_SWE.pdf')).toBeInTheDocument();
    expect(screen.getByText('Active Base Resume')).toBeInTheDocument();
  });

  it('should trigger sample load when clicking Load Senior SWE Sample button', () => {
    const handleLoadSample = vi.fn();

    render(
      <JobSearchControls
        currentResume={mockResume}
        onIngestJobs={vi.fn()}
        onLoadSampleResume={handleLoadSample}
        onUploadResume={vi.fn()}
        isLoading={false}
      />
    );

    const loadButton = screen.getByText('Load Senior SWE Sample');
    fireEvent.click(loadButton);

    expect(handleLoadSample).toHaveBeenCalledTimes(1);
  });

  it('should submit search keywords and locations when submitting the form', () => {
    const handleIngest = vi.fn();

    render(
      <JobSearchControls
        currentResume={mockResume}
        onIngestJobs={handleIngest}
        onLoadSampleResume={vi.fn()}
        onUploadResume={vi.fn()}
        isLoading={false}
      />
    );

    const submitButton = screen.getByText('Ingest & Match Pipeline');
    fireEvent.click(submitButton);

    expect(handleIngest).toHaveBeenCalledTimes(1);
    expect(handleIngest).toHaveBeenCalledWith(
      ['Java', 'Distributed Systems', 'Cloud'],
      ['San Francisco', 'CA / Remote']
    );
  });
});
