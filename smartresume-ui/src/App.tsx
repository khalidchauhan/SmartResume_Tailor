import React, { useState, useEffect } from 'react';
import { Layers, ShieldCheck, FileCheck } from 'lucide-react';
import { BaseResume, JobMatch, TailoredResume, PipelineStats } from './types';
import { api } from './services/api';
import { MetricCards } from './components/dashboard/MetricCards';
import { JobSearchControls } from './components/dashboard/JobSearchControls';
import { JobPipelineTable } from './components/dashboard/JobPipelineTable';
import { MatchBreakdownModal } from './components/matching/MatchBreakdownModal';
import { SideBySideDiffEditor } from './components/tailor/SideBySideDiffEditor';

export const App: React.FC = () => {
  const [currentResume, setCurrentResume] = useState<BaseResume | null>(null);
  const [matches, setMatches] = useState<JobMatch[]>([]);
  const [tailoredResumes, setTailoredResumes] = useState<Map<string, TailoredResume>>(new Map());
  const [selectedMatch, setSelectedMatch] = useState<JobMatch | null>(null);
  const [activeDiffTailored, setActiveDiffTailored] = useState<TailoredResume | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [stats, setStats] = useState<PipelineStats>({
    totalMatches: 0,
    qualifiedCount: 0,
    droppedCount: 0,
    qualificationThreshold: 80,
  });
  const [backendError, setBackendError] = useState<string | null>(null);

  // Initial load
  useEffect(() => {
    initPipeline();
  }, []);

  const initPipeline = async () => {
    setIsLoading(true);
    setBackendError(null);
    try {
      // 1. Fetch or initialize default sample resume
      let resume = await api.getLatestResume();
      if (!resume) {
        resume = await api.loadSampleResume();
      }
      setCurrentResume(resume);

      // 2. Fetch or trigger initial job ingest
      await api.ingestJobs();
      const evaluatedMatches = await api.evaluateAllMatches();
      setMatches(evaluatedMatches || []);

      // 3. Update stats
      const pipelineStats = await api.getStats();
      setStats(pipelineStats);
    } catch (e: any) {
      console.error('Error initializing pipeline:', e);
      setBackendError(e?.message || 'Unable to connect to Spring Boot backend on http://localhost:8080.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleIngestJobs = async (keywords: string[], locations: string[]) => {
    setIsLoading(true);
    try {
      await api.ingestJobs(keywords, locations);
      const evaluated = await api.evaluateAllMatches();
      setMatches(evaluated);
      const pipelineStats = await api.getStats();
      setStats(pipelineStats);
    } catch (e) {
      console.error('Ingestion failed:', e);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLoadSampleResume = async () => {
    setIsLoading(true);
    try {
      const resume = await api.loadSampleResume();
      setCurrentResume(resume);
      const evaluated = await api.evaluateAllMatches();
      setMatches(evaluated);
      const pipelineStats = await api.getStats();
      setStats(pipelineStats);
    } catch (e) {
      console.error('Failed to load sample resume:', e);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUploadResume = async (file: File) => {
    setIsLoading(true);
    try {
      const resume = await api.uploadResume(file);
      setCurrentResume(resume);
      const evaluated = await api.evaluateAllMatches();
      setMatches(evaluated);
      const pipelineStats = await api.getStats();
      setStats(pipelineStats);
    } catch (e) {
      console.error('Upload failed:', e);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTailorMatch = async (matchId: string) => {
    setIsLoading(true);
    try {
      const tailored = await api.generateTailoredResume(matchId);
      setTailoredResumes((prev) => new Map(prev).set(matchId, tailored));
      setActiveDiffTailored(tailored);
    } catch (e: any) {
      alert(e.message || 'Failed to tailor resume');
    } finally {
      setIsLoading(false);
    }
  };

  const handleViewDiff = async (matchId: string) => {
    const existing = tailoredResumes.get(matchId);
    if (existing) {
      setActiveDiffTailored(existing);
      return;
    }
    try {
      const fetched = await api.getTailoredByMatch(matchId);
      if (fetched) {
        setTailoredResumes((prev) => new Map(prev).set(matchId, fetched));
        setActiveDiffTailored(fetched);
      }
    } catch (e) {
      console.error('Failed to load tailored diff:', e);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-['Plus_Jakarta_Sans',sans-serif]">
      {/* Top Navigation */}
      <header className="border-b border-slate-200/90 bg-white/90 backdrop-blur-md sticky top-0 z-40 shadow-2xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center text-white shadow-sm font-black text-sm tracking-tighter">
              ST
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold tracking-tight text-slate-900 text-base">SmartResume</span>
                <span className="text-emerald-700 font-bold text-base">Tailor</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200/80">
                  AI v1.0
                </span>
              </div>
              <p className="text-[11px] text-slate-500 font-medium">Deterministic Matcher & Zero-Hallucination ATS Optimizer</p>
            </div>
          </div>

          <div className="flex items-center gap-3 text-xs">
            <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 border border-slate-200/80 text-slate-600 font-medium">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Strict 80% Filter Policy</span>
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white border border-slate-200/80 text-slate-600 shadow-2xs font-medium">
              <Layers className="w-3.5 h-3.5 text-slate-400" />
              <span>Spring Boot 3.3 + pgvector</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-7">
        {backendError && (
          <div className="bg-amber-50 border border-amber-200/90 rounded-2xl p-5 shadow-sm">
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
              <div className="flex items-start gap-3.5">
                <div className="w-10 h-10 rounded-xl bg-amber-100/80 border border-amber-200 flex items-center justify-center shrink-0 text-amber-700 text-lg">
                  ⚠️
                </div>
                <div>
                  <h3 className="text-sm font-bold text-slate-900">Backend Server Not Detected on Port 8080</h3>
                  <p className="text-xs text-slate-600 mt-1">
                    The frontend Vite proxy failed to reach <code className="bg-amber-100/70 text-amber-900 px-1.5 py-0.5 rounded font-mono text-[11px]">http://localhost:8080</code>.
                    Please launch the Spring Boot backend service in a separate terminal:
                  </p>
                  <pre className="bg-slate-900 text-emerald-400 text-xs p-3 rounded-xl font-mono mt-2.5 overflow-x-auto shadow-inner">
                    cd SmartResume_Tailor/smartresume-api && mvn spring-boot:run
                  </pre>
                </div>
              </div>
              <button
                onClick={initPipeline}
                disabled={isLoading}
                className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-semibold text-xs transition-colors shrink-0 shadow-sm"
              >
                {isLoading ? 'Connecting...' : 'Retry Connection'}
              </button>
            </div>
          </div>
        )}

        {activeDiffTailored ? (
          <SideBySideDiffEditor
            tailoredResume={activeDiffTailored}
            onBack={() => setActiveDiffTailored(null)}
          />
        ) : (
          <>
            {/* Top Metrics Cards */}
            <MetricCards stats={stats} tailoredCount={tailoredResumes.size} />

            {/* Ingestion & Resume Controls */}
            <JobSearchControls
              currentResume={currentResume}
              onIngestJobs={handleIngestJobs}
              onLoadSampleResume={handleLoadSampleResume}
              onUploadResume={handleUploadResume}
              isLoading={isLoading}
            />

            {/* Pipeline Table */}
            <JobPipelineTable
              matches={matches}
              onViewBreakdown={(match) => setSelectedMatch(match)}
              onTailor={handleTailorMatch}
              onViewDiff={handleViewDiff}
              tailoredMatchIds={new Set(tailoredResumes.keys())}
              isLoading={isLoading}
            />
          </>
        )}
      </main>

      {/* Breakdown Modal */}
      <MatchBreakdownModal
        match={selectedMatch}
        onClose={() => setSelectedMatch(null)}
        onProceedToTailor={(matchId: string) => {
          setSelectedMatch(null);
          handleTailorMatch(matchId);
        }}
      />

      {/* Footer */}
      <footer className="border-t border-slate-200/80 bg-white/70 py-6 text-center text-xs text-slate-500">
        SmartResume Tailor • Enterprise AI Job Matcher & Resume Optimization System • Java 21 & React 19
      </footer>
    </div>
  );
};
