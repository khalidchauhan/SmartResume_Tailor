import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { ScoreGauge } from '../../components/matching/ScoreGauge';

describe('ScoreGauge Component', () => {
  it('should render score percentage correctly for qualified score >= 80%', () => {
    render(<ScoreGauge score={85} />);
    const textElement = screen.getByText('85%');
    expect(textElement).toBeInTheDocument();
    expect(textElement.className).toContain('text-emerald-700');
  });

  it('should render score percentage correctly for dropped score < 80%', () => {
    render(<ScoreGauge score={64} />);
    const textElement = screen.getByText('64%');
    expect(textElement).toBeInTheDocument();
    expect(textElement.className).toContain('text-rose-600');
  });
});
