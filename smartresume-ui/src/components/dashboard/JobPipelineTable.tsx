import React, { useState } from 'react';
import { Sparkles, Eye, AlertCircle, CheckCircle2, Building2 } from 'lucide-react';
import { JobMatch } from '../../types';
import { ScoreGauge } from '../matching/ScoreGauge';

interface JobPipelineTableProps {
  matches: JobMatch[];
  onViewBreakdown: (match: JobMatch) => void;
  onTailor: (matchId: string) => void;
  onViewDiff: (matchId: string) => void;
  tailoredMatchIds: Set<string>;
  isLoading: boolean;
}

export const JobPipelineTable: React.FC<JobPipelineTableProps> = ({
  matches,
  onViewBreakdown,
  onTailor,
  onViewDiff,
  tailoredMatchIds,
  isLoading,
}) => {
  const [filter, setFilter] = useState<'ALL' | 'QUALIFIED' | 'DROPPED'>('ALL');

  const safeMatches = Array.isArray(matches) ? matches : [];

  const filteredMatches = safeMatches.filter((m) => {
    if (filter === 'QUALIFIED') return m.status === 'QUALIFIED';
    if (filter === 'DROPPED') return m.status === 'DROPPED_LOW_MATCH';
    return true;
  });

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl overflow-hidden shadow-sm">
      {/* Table Header Controls */}
      <div className="p-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-bold text-slate-900">Active Job Pipeline</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time semantic compatibility evaluation against active base resume
          </p>
        </div>

        {/* Filter Segmented Control */}
        <div className="flex items-center gap-1 bg-slate-100/90 p-1 rounded-xl border border-slate-200/60">
          <button
            onClick={() => setFilter('ALL')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === 'ALL'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            All ({safeMatches.length})
          </button>
          <button
            onClick={() => setFilter('QUALIFIED')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === 'QUALIFIED'
                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/80 shadow-xs'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            Qualified ≥80% ({safeMatches.filter((m) => m.status === 'QUALIFIED').length})
          </button>
          <button
            onClick={() => setFilter('DROPPED')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              filter === 'DROPPED'
                ? 'bg-rose-50 text-rose-700 border border-rose-200/80 shadow-xs'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            Dropped &lt;80% ({safeMatches.filter((m) => m.status === 'DROPPED_LOW_MATCH').length})
          </button>
        </div>
      </div>

      {/* Table Rows */}
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50/60 text-[11px] uppercase tracking-wider text-slate-500 font-bold">
              <th className="py-3.5 px-5">Role & Company</th>
              <th className="py-3.5 px-4 text-center">Score</th>
              <th className="py-3.5 px-4">Threshold Status</th>
              <th className="py-3.5 px-4">Evaluation Summary</th>
              <th className="py-3.5 px-5 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-xs">
            {filteredMatches.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-12 text-center text-slate-400">
                  No jobs found in pipeline. Click "Ingest & Match Pipeline" above to load roles.
                </td>
              </tr>
            ) : (
              filteredMatches.map((m) => {
                const isQualified = m.status === 'QUALIFIED';
                const hasTailored = tailoredMatchIds.has(m.id);

                return (
                  <tr
                    key={m.id}
                    className="hover:bg-slate-50/70 transition-colors group cursor-pointer"
                    onClick={() => onViewBreakdown(m)}
                  >
                    {/* Role & Company */}
                    <td className="py-4 px-5">
                      <div className="font-bold text-slate-900 text-sm group-hover:text-emerald-700 transition-colors">
                        {m.job.title}
                      </div>
                      <div className="text-slate-500 text-xs mt-0.5 flex items-center gap-2">
                        <span className="font-medium text-slate-700 flex items-center gap-1">
                          <Building2 className="w-3.5 h-3.5 text-slate-400" />
                          {m.job.company}
                        </span>
                        <span>•</span>
                        <span>{m.job.location}</span>
                        {m.job.isRemote && (
                          <span className="px-1.5 py-0.2 rounded text-[10px] bg-blue-50 text-blue-700 border border-blue-200/80 font-medium">
                            Remote
                          </span>
                        )}
                      </div>
                    </td>

                    {/* Compatibility Score */}
                    <td className="py-4 px-4 text-center">
                      <ScoreGauge score={m.overallScore} size={46} strokeWidth={5} />
                    </td>

                    {/* Status Badge */}
                    <td className="py-4 px-4">
                      {isQualified ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                          QUALIFIED (≥ 80%)
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-50 text-rose-700 border border-rose-200">
                          <AlertCircle className="w-3.5 h-3.5 text-rose-500" />
                          DROPPED (&lt; 80%)
                        </span>
                      )}
                    </td>

                    {/* Key Verdict */}
                    <td className="py-4 px-4 max-w-xs truncate text-slate-600 text-xs">
                      {isQualified ? (
                        <span className="text-emerald-800 font-medium">
                          Skills {m.skillsScore}/35 • Exp {m.experienceScore}/30 • Domain {m.domainScore}/20
                        </span>
                      ) : (
                        <span className="text-slate-500 italic">{m.archiveReason}</span>
                      )}
                    </td>

                    {/* Actions */}
                    <td
                      className="py-4 px-5 text-right space-x-2"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <button
                        onClick={() => onViewBreakdown(m)}
                        className="px-3 py-1.5 rounded-lg bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold border border-slate-200 shadow-2xs transition-all inline-flex items-center gap-1.5"
                      >
                        <Eye className="w-3.5 h-3.5 text-slate-400" />
                        Breakdown
                      </button>

                      {isQualified && (
                        hasTailored ? (
                          <button
                            onClick={() => onViewDiff(m.id)}
                            className="px-3 py-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100/80 text-indigo-700 text-xs font-bold border border-indigo-200/80 shadow-2xs transition-all inline-flex items-center gap-1.5"
                          >
                            <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
                            Review Diff
                          </button>
                        ) : (
                          <button
                            onClick={() => onTailor(m.id)}
                            className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm shadow-emerald-600/20 transition-all inline-flex items-center gap-1.5 hover:translate-y-[-1px] active:translate-y-[0px]"
                          >
                            <Sparkles className="w-3.5 h-3.5 text-white" />
                            Tailor Resume
                          </button>
                        )
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
