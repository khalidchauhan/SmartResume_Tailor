import React from 'react';
import { Briefcase, CheckCircle2, XCircle, Sparkles } from 'lucide-react';
import { PipelineStats } from '../../types';

interface MetricCardsProps {
  stats: PipelineStats;
  tailoredCount: number;
}

export const MetricCards: React.FC<MetricCardsProps> = ({ stats, tailoredCount }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Ingested */}
      <div className="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200 group">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Jobs Ingested</p>
            <h3 className="text-3xl font-extrabold text-slate-900 mt-1.5">{stats.totalMatches}</h3>
          </div>
          <div className="w-11 h-11 rounded-xl bg-blue-50 border border-blue-100/80 flex items-center justify-center text-blue-600 group-hover:scale-105 transition-transform">
            <Briefcase className="w-5 h-5" />
          </div>
        </div>
        <div className="mt-3 flex items-center gap-1.5 text-xs text-slate-500">
          <span className="w-1.5 h-1.5 rounded-full bg-blue-500" />
          <span>Active search pipeline</span>
        </div>
      </div>

      {/* Qualified (>= 80%) */}
      <div className="bg-white border border-emerald-200/80 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200 group relative overflow-hidden">
        <div className="absolute top-0 right-0 w-24 h-24 bg-emerald-50/50 rounded-full blur-2xl -mr-6 -mt-6 pointer-events-none" />
        <div className="flex items-center justify-between relative">
          <div>
            <p className="text-xs font-semibold text-emerald-700 uppercase tracking-wider flex items-center gap-1">
              Qualified (≥ 80%)
            </p>
            <h3 className="text-3xl font-extrabold text-emerald-700 mt-1.5">{stats.qualifiedCount}</h3>
          </div>
          <div className="w-11 h-11 rounded-xl bg-emerald-50 border border-emerald-200/60 flex items-center justify-center text-emerald-600 group-hover:scale-105 transition-transform">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>
        <div className="mt-3 flex items-center gap-1.5 text-xs text-emerald-600 font-medium">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
          <span>Eligible for resume tailoring</span>
        </div>
      </div>

      {/* Dropped (< 80%) */}
      <div className="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200 group">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Dropped (&lt; 80%)</p>
            <h3 className="text-3xl font-extrabold text-slate-700 mt-1.5">{stats.droppedCount}</h3>
          </div>
          <div className="w-11 h-11 rounded-xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-600 group-hover:scale-105 transition-transform">
            <XCircle className="w-5 h-5" />
          </div>
        </div>
        <div className="mt-3 flex items-center gap-1.5 text-xs text-slate-500">
          <span className="w-1.5 h-1.5 rounded-full bg-rose-400" />
          <span>Archived automatically</span>
        </div>
      </div>

      {/* Tailored Resumes */}
      <div className="bg-white border border-indigo-200/80 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200 group relative overflow-hidden">
        <div className="absolute top-0 right-0 w-24 h-24 bg-indigo-50/50 rounded-full blur-2xl -mr-6 -mt-6 pointer-events-none" />
        <div className="flex items-center justify-between relative">
          <div>
            <p className="text-xs font-semibold text-indigo-700 uppercase tracking-wider">Tailored Resumes</p>
            <h3 className="text-3xl font-extrabold text-indigo-900 mt-1.5">{tailoredCount}</h3>
          </div>
          <div className="w-11 h-11 rounded-xl bg-indigo-50 border border-indigo-200/60 flex items-center justify-center text-indigo-600 group-hover:scale-105 transition-transform">
            <Sparkles className="w-5 h-5" />
          </div>
        </div>
        <div className="mt-3 flex items-center gap-1.5 text-xs text-indigo-600 font-medium">
          <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
          <span>ATS optimized & ready</span>
        </div>
      </div>
    </div>
  );
};
