import React, { useState } from 'react';
import { Download, Check, X, Sparkles, ArrowLeft, FileText, Tag, CheckCircle2 } from 'lucide-react';
import { TailoredResume, DiffItemDto, TailoredResumeDto } from '../../types';
import { api } from '../../services/api';

interface SideBySideDiffEditorProps {
  tailoredResume: TailoredResume;
  onBack: () => void;
}

export const SideBySideDiffEditor: React.FC<SideBySideDiffEditorProps> = ({
  tailoredResume,
  onBack,
}) => {
  let resumeDto: TailoredResumeDto | null = null;
  try {
    resumeDto = JSON.parse(tailoredResume.tailoredJson);
  } catch {
    resumeDto = null;
  }

  const [diffItems, setDiffItems] = useState<DiffItemDto[]>(() => {
    try {
      return JSON.parse(tailoredResume.diffJson);
    } catch {
      return [];
    }
  });

  const toggleAcceptDiff = (id: string) => {
    setDiffItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, accepted: !item.accepted } : item))
    );
  };

  const handleDownloadPdf = () => {
    window.open(api.getExportPdfUrl(tailoredResume.id), '_blank');
  };

  const handleDownloadDocx = () => {
    window.open(api.getExportDocxUrl(tailoredResume.id), '_blank');
  };

  if (!resumeDto) {
    return <div className="p-8 text-center text-slate-500">Failed to render tailored resume.</div>;
  }

  return (
    <div className="space-y-6">
      {/* Top Action Bar */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-5 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3.5">
          <button
            onClick={onBack}
            className="p-2.5 rounded-xl bg-slate-50 hover:bg-slate-100 text-slate-600 border border-slate-200 shadow-2xs transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200/80">
                TAILORED REFINEMENT
              </span>
              <span className="text-xs text-slate-500">Target Role:</span>
              <span className="text-xs font-bold text-slate-800">
                {tailoredResume.jobMatch.job.title} ({tailoredResume.jobMatch.job.company})
              </span>
            </div>
            <h1 className="text-lg font-extrabold text-slate-900 mt-0.5">Side-by-Side ATS Comparison & Diff</h1>
          </div>
        </div>

        {/* ATS Score & Export Actions */}
        <div className="flex items-center gap-2.5">
          <div className="px-3.5 py-1.5 rounded-xl bg-indigo-50/80 border border-indigo-200/80 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-indigo-600" />
            <div className="text-left">
              <span className="text-[10px] uppercase text-indigo-600/80 font-bold block leading-none">
                ATS Score
              </span>
              <span className="text-sm font-extrabold text-indigo-900 font-mono">
                {tailoredResume.atsScore}%
              </span>
            </div>
          </div>

          <button
            onClick={handleDownloadPdf}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-sm shadow-emerald-600/20 transition-all flex items-center gap-1.5 hover:translate-y-[-1px] active:translate-y-[0px]"
          >
            <Download className="w-3.5 h-3.5" />
            Export Clean PDF
          </button>

          <button
            onClick={handleDownloadDocx}
            className="px-4 py-2 bg-white hover:bg-slate-50 text-slate-700 text-xs font-bold rounded-xl border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <FileText className="w-3.5 h-3.5 text-slate-500" />
            Export Word (DOCX)
          </button>
        </div>
      </div>

      {/* Dual-Pane View */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Pane: Original Base Resume */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-sm">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-700 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-slate-400" />
              Original Base Resume
            </h3>
            <span className="text-xs text-slate-400 font-mono">Source Baseline</span>
          </div>

          <div className="mt-5 space-y-5 text-xs leading-relaxed text-slate-700">
            <div>
              <h2 className="text-base font-bold text-slate-900">{resumeDto.candidateName}</h2>
              <p className="text-slate-500 mt-0.5">
                {resumeDto.email} • {resumeDto.phone} • {resumeDto.location}
              </p>
            </div>

            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/80">
              <h4 className="text-[11px] font-bold text-slate-600 uppercase tracking-wider mb-1.5">
                Executive Summary
              </h4>
              <p className="text-slate-600">
                Experienced software engineer with 8+ years building high-scale distributed backend
                systems, event-driven architectures, and cloud services in Java and Spring Boot.
              </p>
            </div>

            <div className="space-y-3.5">
              <h4 className="text-[11px] font-bold text-slate-600 uppercase tracking-wider">
                Work Experience
              </h4>
              <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/80 space-y-2">
                <div className="flex justify-between font-bold text-slate-800">
                  <span>Senior Software Engineer — Tech Corp</span>
                  <span className="text-slate-500 font-normal">2021 - Present</span>
                </div>
                <ul className="list-disc list-inside space-y-1.5 text-slate-600">
                  <li>Architected event-driven microservices processing 120k RPM with 99.99% availability.</li>
                  <li>Reduced p99 query latency by 42% through PostgreSQL query plan tuning and Redis caching.</li>
                  <li>Mentored 4 junior engineers and led migration of core services to Kubernetes on AWS.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        {/* Right Pane: Tailored ATS-Optimized Resume with Interactive Diff */}
        <div className="bg-white border border-indigo-200/90 rounded-2xl p-6 shadow-sm relative">
          <div className="flex items-center justify-between pb-4 border-b border-indigo-100">
            <h3 className="text-sm font-bold text-indigo-900 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-indigo-600" />
              Tailored ATS-Optimized Resume
            </h3>
            <span className="text-xs text-indigo-700 font-mono font-semibold">
              {diffItems.length} Smart Refinements
            </span>
          </div>

          <div className="mt-5 space-y-5 text-xs leading-relaxed">
            <div>
              <h2 className="text-base font-bold text-slate-900">{resumeDto.candidateName}</h2>
              <p className="text-indigo-700 font-semibold mt-0.5">
                {resumeDto.tailoredHeadline}
              </p>
            </div>

            {/* Tailored Summary */}
            <div className="bg-indigo-50/50 border border-indigo-200/80 p-4 rounded-xl">
              <div className="flex items-center justify-between mb-1.5">
                <h4 className="text-[11px] font-bold text-indigo-900 uppercase tracking-wider flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-indigo-600" /> Refined Professional Summary
                </h4>
                <span className="px-2 py-0.5 rounded text-[10px] bg-white text-indigo-700 border border-indigo-200 font-bold shadow-2xs">
                  +3 Keywords
                </span>
              </div>
              <p className="text-slate-800 leading-relaxed font-normal">{resumeDto.tailoredSummary}</p>
            </div>

            {/* Granular Diff Items with Accept / Revert Controls */}
            <div className="space-y-3">
              <h4 className="text-[11px] font-bold text-slate-600 uppercase tracking-wider">
                Refined Bullet Points (Google XYZ Formula)
              </h4>

              {diffItems
                .filter((d) => d.section === 'WORK_EXPERIENCE')
                .map((item) => (
                  <div
                    key={item.id}
                    className={`p-4 rounded-xl border transition-all ${
                      item.accepted
                        ? 'bg-emerald-50/40 border-emerald-200/90 shadow-2xs'
                        : 'bg-slate-50 border-slate-200 opacity-60'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <span className="text-[10px] font-mono text-slate-500 block mb-1">
                          {item.context}
                        </span>
                        <p
                          className={`text-xs leading-relaxed ${
                            item.accepted ? 'text-emerald-950 font-medium' : 'text-slate-400 line-through'
                          }`}
                        >
                          {item.tailoredText}
                        </p>
                      </div>

                      {/* Accept/Revert Toggle */}
                      <button
                        onClick={() => toggleAcceptDiff(item.id)}
                        className={`p-1.5 rounded-lg border text-xs transition-colors flex-shrink-0 ${
                          item.accepted
                            ? 'bg-white text-emerald-700 border-emerald-300 hover:bg-rose-50 hover:text-rose-700 hover:border-rose-300'
                            : 'bg-white text-slate-600 border-slate-300 hover:text-slate-900'
                        }`}
                        title={item.accepted ? 'Revert to original' : 'Accept tailored rewrite'}
                      >
                        {item.accepted ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />}
                      </button>
                    </div>

                    {/* Keywords Injected Tag Cloud */}
                    {item.injectedKeywords.length > 0 && (
                      <div className="flex flex-wrap items-center gap-1.5 mt-3 pt-2.5 border-t border-emerald-200/60">
                        <Tag className="w-3 h-3 text-emerald-600" />
                        <span className="text-[10px] text-slate-500 font-medium">Injected:</span>
                        {item.injectedKeywords.map((kw, kwIdx) => (
                          <span
                            key={kwIdx}
                            className="px-2 py-0.5 rounded text-[10px] bg-white text-emerald-800 border border-emerald-200 font-semibold shadow-2xs"
                          >
                            +{kw}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
