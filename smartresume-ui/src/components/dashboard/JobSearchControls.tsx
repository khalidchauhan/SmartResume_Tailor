import React, { useState } from 'react';
import { Search, MapPin, Sparkles, FileText, Upload, RefreshCw, CheckCircle2 } from 'lucide-react';
import { BaseResume } from '../../types';

interface JobSearchControlsProps {
  currentResume: BaseResume | null;
  onIngestJobs: (keywords: string[], locations: string[]) => Promise<void>;
  onLoadSampleResume: () => Promise<void>;
  onUploadResume: (file: File) => Promise<void>;
  isLoading: boolean;
}

export const JobSearchControls: React.FC<JobSearchControlsProps> = ({
  currentResume,
  onIngestJobs,
  onLoadSampleResume,
  onUploadResume,
  isLoading,
}) => {
  const [keywords, setKeywords] = useState('Java, Distributed Systems, Cloud');
  const [location, setLocation] = useState('San Francisco, CA / Remote');

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const kwList = keywords.split(',').map((k) => k.trim()).filter(Boolean);
    const locList = location.split(',').map((l) => l.trim()).filter(Boolean);
    onIngestJobs(kwList, locList);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      onUploadResume(e.target.files[0]);
    }
  };

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-sm">
      {/* Top Bar: Active Resume & Ingestion Tools */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-slate-100">
        <div className="flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600">
            <FileText className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-slate-800">Active Base Resume</h2>
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200/80">
                <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                Active Source
              </span>
            </div>
            <p className="text-xs text-slate-500 font-mono mt-0.5">
              {currentResume ? currentResume.fileName : 'No resume loaded yet'}
            </p>
          </div>
        </div>

        {/* Action Buttons for Resume */}
        <div className="flex items-center gap-2.5">
          <label className="cursor-pointer px-3.5 py-2 rounded-xl bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-semibold border border-slate-200 transition-all flex items-center gap-2 shadow-2xs">
            <Upload className="w-3.5 h-3.5 text-slate-500" />
            Upload PDF / DOCX
            <input type="file" accept=".pdf,.docx" className="hidden" onChange={handleFileChange} />
          </label>
          <button
            onClick={onLoadSampleResume}
            disabled={isLoading}
            className="px-3.5 py-2 rounded-xl bg-emerald-50 hover:bg-emerald-100/80 text-emerald-700 text-xs font-semibold border border-emerald-200/80 transition-all flex items-center gap-2 shadow-2xs"
          >
            <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
            Load Senior SWE Sample
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 md:grid-cols-12 gap-3.5 mt-5">
        <div className="md:col-span-5 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={keywords}
            onChange={(e) => setKeywords(e.target.value)}
            placeholder="Target Keywords (e.g. Java 21, Kafka, Distributed Systems)"
            className="w-full pl-9 pr-4 py-2.5 bg-slate-50/70 hover:bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-slate-400 focus:ring-2 focus:ring-slate-900/5 transition-all"
          />
        </div>

        <div className="md:col-span-4 relative">
          <MapPin className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="Location (e.g. Remote, San Francisco)"
            className="w-full pl-9 pr-4 py-2.5 bg-slate-50/70 hover:bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-slate-400 focus:ring-2 focus:ring-slate-900/5 transition-all"
          />
        </div>

        <div className="md:col-span-3">
          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-2.5 bg-slate-900 hover:bg-slate-800 disabled:opacity-50 text-white font-semibold text-xs rounded-xl shadow-sm transition-all flex items-center justify-center gap-2 hover:translate-y-[-1px] active:translate-y-[0px]"
          >
            {isLoading ? (
              <RefreshCw className="w-4 h-4 animate-spin text-slate-300" />
            ) : (
              <Sparkles className="w-4 h-4 text-emerald-400" />
            )}
            Ingest & Match Pipeline
          </button>
        </div>
      </form>
    </div>
  );
};
