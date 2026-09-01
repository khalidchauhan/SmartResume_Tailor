import { BaseResume, JobPosting, JobMatch, TailoredResume, PipelineStats, TailoredResumeDto } from '../types';

const API_BASE = '/api/v1';

export const api = {
  async getLatestResume(): Promise<BaseResume | null> {
    try {
      const res = await fetch(`${API_BASE}/resumes/latest`);
      if (!res.ok) return null;
      const json = await res.json();
      return json.data;
    } catch {
      return null;
    }
  },

  async loadSampleResume(): Promise<BaseResume> {
    const res = await fetch(`${API_BASE}/resumes/sample`, { method: 'POST' });
    if (!res.ok) {
      throw new Error(`Failed to load sample resume (HTTP ${res.status}). Ensure Spring Boot backend is running on port 8080.`);
    }
    const json = await res.json();
    return json.data;
  },

  async uploadResume(file: File): Promise<BaseResume> {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/resumes/upload`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) {
      throw new Error(`Failed to upload resume (HTTP ${res.status}). Ensure Spring Boot backend is running on port 8080.`);
    }
    const json = await res.json();
    return json.data;
  },

  async ingestJobs(keywords: string[] = ['Java', 'Distributed Systems'], locations: string[] = ['Remote']): Promise<JobPosting[]> {
    const res = await fetch(`${API_BASE}/jobs/ingest`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ keywords, locations, limit: 10 }),
    });
    if (!res.ok) {
      throw new Error(`Failed to ingest jobs (HTTP ${res.status}). Ensure Spring Boot backend is running on port 8080.`);
    }
    const json = await res.json();
    return json.data || [];
  },

  async evaluateAllMatches(): Promise<JobMatch[]> {
    const res = await fetch(`${API_BASE}/matches/evaluate-all`, { method: 'POST' });
    if (!res.ok) {
      throw new Error(`Failed to evaluate matches (HTTP ${res.status}). Ensure Spring Boot backend is running on port 8080.`);
    }
    const json = await res.json();
    return json.data || [];
  },

  async getAllMatches(): Promise<JobMatch[]> {
    const res = await fetch(`${API_BASE}/matches`);
    if (!res.ok) return [];
    const json = await res.json();
    return json.data || [];
  },

  async getStats(): Promise<PipelineStats> {
    const res = await fetch(`${API_BASE}/matches/stats`);
    if (!res.ok) {
      return { totalMatches: 0, qualifiedCount: 0, droppedCount: 0, qualificationThreshold: 80 };
    }
    const json = await res.json();
    return json.data || { totalMatches: 0, qualifiedCount: 0, droppedCount: 0, qualificationThreshold: 80 };
  },

  async generateTailoredResume(matchId: string): Promise<TailoredResume> {
    const res = await fetch(`${API_BASE}/tailor/${matchId}/generate`, { method: 'POST' });
    const json = await res.json();
    if (!json.success) {
      throw new Error(json.message || 'Failed to tailor resume');
    }
    return json.data;
  },

  async getTailoredByMatch(matchId: string): Promise<TailoredResume | null> {
    try {
      const res = await fetch(`${API_BASE}/tailor/${matchId}`);
      if (!res.ok) return null;
      const json = await res.json();
      return json.data;
    } catch {
      return null;
    }
  },

  getExportPdfUrl(tailoredResumeId: string): string {
    return `${API_BASE}/export/${tailoredResumeId}/pdf`;
  },

  getExportDocxUrl(tailoredResumeId: string): string {
    return `${API_BASE}/export/${tailoredResumeId}/docx`;
  }
};
