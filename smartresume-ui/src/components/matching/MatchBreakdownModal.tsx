import React from 'react';
import { X, CheckCircle2, AlertTriangle, ArrowRight, ShieldCheck, Ban } from 'lucide-react';
import { JobMatch, MatchEvaluationDto } from '../../types';
import { ScoreGauge } from './ScoreGauge';

interface MatchBreakdownModalProps {
  match: JobMatch | null;
  onClose: () => void;
  onProceedToTailor: (matchId: string) => void;
}

export const MatchBreakdownModal: React.FC<MatchBreakdownModalProps> = ({
  match,
  onClose,
  onProceedToTailor,
}) => {
  if (!match) return null;

  let evaluation: MatchEvaluationDto | null = null;
  try {
    evaluation = JSON.parse(match.evaluationJson);
  } catch {
    evaluation = null;
  }

  const isQualified = match.overallScore >= 80;

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
      <div className="bg-white border border-slate-200 rounded-2xl max-w-2xl w-full p-6 shadow-2xl relative animate-in fade-in zoom-in-95 duration-150">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 w-8 h-8 rounded-lg bg-slate-100 text-slate-400 hover:text-slate-700 hover:bg-slate-200 flex items-center justify-center transition-colors"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Header */}
        <div className="flex items-start gap-4">
          <ScoreGauge score={match.overallScore} size={64} strokeWidth={6} />
          <div>
            <div className="flex items-center gap-2">
              <span
                className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                  isQualified
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                    : 'bg-rose-50 text-rose-700 border-rose-200'
                }`}
              >
                {isQualified ? 'QUALIFIED (≥ 80% THRESHOLD MET)' : 'DROPPED / LOW MATCH (< 80%)'}
              </span>
            </div>
            <h2 className="text-xl font-bold text-slate-900 mt-1.5">{match.job.title}</h2>
            <p className="text-xs text-slate-500 font-medium">
              {match.job.company} • {match.job.location}
            </p>
          </div>
        </div>

        {/* Reason Banner */}
        <div
          className={`mt-5 p-4 rounded-xl border text-xs leading-relaxed ${
            isQualified
              ? 'bg-emerald-50/80 border-emerald-200 text-emerald-900'
              : 'bg-rose-50/80 border-rose-200 text-rose-900'
          }`}
        >
          <div className="flex items-center gap-2 font-bold mb-1">
            {isQualified ? (
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
            ) : (
              <Ban className="w-4 h-4 text-rose-600" />
            )}
            <span>{isQualified ? 'Qualification Summary' : 'Archive Rationale'}</span>
          </div>
          <p className="font-normal">{evaluation?.verdictReason || match.archiveReason}</p>
        </div>

        {/* Category Breakdown Progress Bars */}
        <div className="mt-5 space-y-3.5">
          <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider">
            Quantitative Score Breakdown
          </h4>

          {/* Skills Score */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-700 font-medium">Skills & Tech Stack Alignment</span>
              <span className="font-mono text-emerald-700 font-bold">
                {match.skillsScore} / 35 pts
              </span>
            </div>
            <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-emerald-500 rounded-full transition-all duration-500"
                style={{ width: `${(match.skillsScore / 35) * 100}%` }}
              />
            </div>
          </div>

          {/* Experience Score */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-700 font-medium">Experience & Seniority Level</span>
              <span className="font-mono text-blue-700 font-bold">
                {match.experienceScore} / 30 pts
              </span>
            </div>
            <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-blue-500 rounded-full transition-all duration-500"
                style={{ width: `${(match.experienceScore / 30) * 100}%` }}
              />
            </div>
          </div>

          {/* Domain Score */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-700 font-medium">Domain & Architecture Alignment</span>
              <span className="font-mono text-indigo-700 font-bold">
                {match.domainScore} / 20 pts
              </span>
            </div>
            <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-indigo-500 rounded-full transition-all duration-500"
                style={{ width: `${(match.domainScore / 20) * 100}%` }}
              />
            </div>
          </div>

          {/* Education Score */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-700 font-medium">Education & Baseline Criteria</span>
              <span className="font-mono text-amber-700 font-bold">
                {match.educationScore} / 15 pts
              </span>
            </div>
            <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-amber-500 rounded-full transition-all duration-500"
                style={{ width: `${(match.educationScore / 15) * 100}%` }}
              />
            </div>
          </div>
        </div>

        {/* Skills Match vs Missing Chips */}
        {evaluation && (
          <div className="mt-5 grid grid-cols-1 md:grid-cols-2 gap-3.5">
            <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200/80">
              <p className="text-[11px] font-bold text-emerald-800 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> Matched Skills
              </p>
              <div className="flex flex-wrap gap-1.5">
                {evaluation.matchedSkills.map((s, idx) => (
                  <span
                    key={idx}
                    className="px-2 py-0.5 rounded-md text-[11px] bg-white text-emerald-800 border border-emerald-200 font-medium shadow-2xs"
                  >
                    {s}
                  </span>
                ))}
              </div>
            </div>

            <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200/80">
              <p className="text-[11px] font-bold text-rose-800 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <AlertTriangle className="w-3.5 h-3.5 text-rose-600" /> Critical Missing Gaps
              </p>
              <div className="flex flex-wrap gap-1.5">
                {evaluation.criticalMissingSkills.length > 0 ? (
                  evaluation.criticalMissingSkills.map((s, idx) => (
                    <span
                      key={idx}
                      className="px-2 py-0.5 rounded-md text-[11px] bg-white text-rose-800 border border-rose-200 font-medium shadow-2xs"
                    >
                      {s}
                    </span>
                  ))
                ) : (
                  <span className="text-xs text-slate-400">None detected</span>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Modal Footer */}
        <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end gap-2.5">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-white hover:bg-slate-100 text-slate-700 text-xs font-semibold border border-slate-200 shadow-2xs transition-colors"
          >
            Close
          </button>
          {isQualified && (
            <button
              onClick={() => {
                onClose();
                onProceedToTailor(match.id);
              }}
              className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-sm shadow-emerald-600/20 transition-all flex items-center gap-2"
            >
              Proceed to Resume Refinement
              <ArrowRight className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
