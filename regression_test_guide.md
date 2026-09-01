# 🧭 SmartResume Tailor — Regression Testing & Functional Guide
### A Guide for Engineers & Users Working on the Application

This guide explains the automated regression test suites and provides a step-by-step walkthrough of the core functionality.

---

## 🎯 What the Regression Suites Prove

The regression test suites validate the 5 non-negotiable business rules of SmartResume Tailor:

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ 1. INGESTION    │ ───>  │ 2. MATCH ENGINE │ ───>  │ 3. 80% GATE     │ ───>  │ 4. REFINEMENT   │ ───> 5. EXPORT
│ Resume & JDs    │       │ Skills   (35%)  │       │ < 80%: ARCHIVE  │       │ Google XYZ      │      Clean PDF
│ Lossless Parse  │       │ Exp      (30%)  │       │ ≥ 80%: QUALIFY  │       │ Zero-Fab Diff   │      Clean DOCX
│                 │       │ Domain   (20%)  │       │                 │       │                 │
│                 │       │ Edu/Cert (15%)  │       │                 │       │                 │
└─────────────────┘       └─────────────────┘       └─────────────────┘       └─────────────────┘
```

1. **Lossless Ingestion**: Resumes uploaded in PDF or DOCX format have their text, contact info, skills, and experience extracted accurately.
2. **Deterministic Matching**: Evaluates candidate compatibility using an objective 0–100% scoring rubric.
3. **Strict 80% Filter Gate**:
   - **Score < 80%**: Marked `DROPPED_LOW_MATCH` and archived with an explicit explanation of missing critical gaps.
   - **Score ≥ 80%**: Marked `QUALIFIED` and eligible for resume refinement.
4. **Zero-Hallucination Refinement**: Low matches cannot be refined under any circumstance. Qualified matches are rewritten using Google's XYZ formula ("Accomplished [X], as measured by [Y], by doing [Z]") without inventing experience, outputting granular before/after diffs.
5. **ATS-Compliant Document Export**: Exports single-column, cleanly structured PDFs and Word DOCX files.

---

## ⚡ Quick Test Execution

### 1. Run Backend Tests (21 Tests)
```bash
cd SmartResume_Tailor/smartresume-api
mvn test
```
**Expected Output**:
```text
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2. Run Frontend Tests (15 Tests)
```bash
cd SmartResume_Tailor/smartresume-ui
npm test
```
**Expected Output**:
```text
Test Files  7 passed (7)
     Tests  15 passed (15)
```

---

## 🧪 Detailed Test Coverage Breakdown

### Backend Service Tests (`smartresume-api`)

| Test File | Covered Functionality |
|---|---|
| [`regression/SmartResumeRegressionE2ETest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/regression/SmartResumeRegressionE2ETest.java) | **End-to-End Regression**: Resume Ingest $\rightarrow$ Job Ingest $\rightarrow$ 80% Gate check $\rightarrow$ Low-match block $\rightarrow$ Qualified refinement $\rightarrow$ PDF/DOCX binary export |
| [`matcher/SemanticMatcherServiceTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/matcher/SemanticMatcherServiceTest.java) | Tests 0-100% calculation across Skills (35%), Experience (30%), Domain (20%), Education (15%) |
| [`refinement/ResumeRefinementServiceTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/refinement/ResumeRefinementServiceTest.java) | Tests `IllegalStateException` on matches $< 80\%$ and successful diff generation on $\ge 80\%$ |
| [`ingestion/ResumeParserServiceTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/ingestion/ResumeParserServiceTest.java) | Tests raw text extraction, skills heuristics, and multipart PDF parsing |
| [`ingestion/JobIngestionServiceTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/ingestion/JobIngestionServiceTest.java) | Tests catalog loading, deduplication, and requirement extraction |
| [`export/DocumentExportServiceTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/export/DocumentExportServiceTest.java) | Tests valid PDF and DOCX binary byte generation |
| [`ingestion/ResumeControllerTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/ingestion/ResumeControllerTest.java) | Tests upload endpoints, sample loading, and latest resume query |
| [`ingestion/JobControllerTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/ingestion/JobControllerTest.java) | Tests job ingestion triggering and catalog retrieval |
| [`matcher/MatchControllerTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/matcher/MatchControllerTest.java) | Tests match evaluation endpoints and pipeline statistics counts |
| [`refinement/TailorControllerTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/refinement/TailorControllerTest.java) | Tests HTTP 403 Forbidden enforcement on matches $< 80\%$ |
| [`export/ExportControllerTest.java`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-api/src/test/java/com/smartresume/tailor/export/ExportControllerTest.java) | Tests streaming binary file downloads with `Content-Disposition` headers |

---

### Frontend UI Tests (`smartresume-ui`)

| Test File | Covered Functionality |
|---|---|
| [`test/automation/FrontendAppAutomation.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/automation/FrontendAppAutomation.test.tsx) | **Full Interactive App Automation**: Realistic user keystrokes for search keywords & location $\rightarrow$ Resume file upload $\rightarrow$ Filter tab switching $\rightarrow$ Modal inspection $\rightarrow$ Tailoring trigger $\rightarrow$ Diff bullet toggling $\rightarrow$ PDF/DOCX download triggers |
| [`test/automation/UserWorkflowRegression.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/automation/UserWorkflowRegression.test.tsx) | **Full User Journey Regression**: Dashboard load $\rightarrow$ Filter qualified $\rightarrow$ Open breakdown modal $\rightarrow$ Proceed to tailor $\rightarrow$ Side-by-Side Diff $\rightarrow$ Toggle rewrites $\rightarrow$ Export validation $\rightarrow$ Navigate back |
| [`test/dashboard/MetricCards.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/dashboard/MetricCards.test.tsx) | Tests metric counts for Ingested, Qualified, Dropped, and Tailored jobs |
| [`test/dashboard/JobSearchControls.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/dashboard/JobSearchControls.test.tsx) | Tests keyword input, location filtering, file upload, and sample load |
| [`test/dashboard/JobPipelineTable.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/dashboard/JobPipelineTable.test.tsx) | Tests row rendering, tab filtering (`ALL`, `QUALIFIED`, `DROPPED`), and action callbacks |
| [`test/matching/ScoreGauge.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/matching/ScoreGauge.test.tsx) | Tests SVG circular progress rendering and emerald vs. rose threshold styling |
| [`test/matching/MatchBreakdownModal.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/matching/MatchBreakdownModal.test.tsx) | Tests 4-part score breakdown bars, skill chips, and conditional "Proceed" button |
| [`test/tailor/SideBySideDiffEditor.test.tsx`](file:///Users/khalidchauhan/Downloads/Antigravity_Workspace/SmartResume_Tailor/smartresume-ui/src/test/tailor/SideBySideDiffEditor.test.tsx) | Tests dual-pane documents, bullet accept/revert state toggles, and PDF/DOCX downloads |

---

## 🚀 Manual Step-by-Step Walkthrough for New Users

Follow these steps to experience the application in your browser:

### Step 1: Start the Servers
```bash
# Terminal 1: Backend
cd SmartResume_Tailor/smartresume-api
mvn spring-boot:run

# Terminal 2: Frontend
cd SmartResume_Tailor/smartresume-ui
npm run dev
```
Open **`http://localhost:5173`** in your browser.

### Step 2: Ingest & Inspect the Pipeline
1. In the top bar, click **"Load Senior SWE Sample"** (or upload your own PDF/DOCX resume).
2. Click **"Ingest & Match Pipeline"**.
3. Observe the metric cards update:
   - **Jobs Ingested**: e.g., 4
   - **Qualified (≥ 80%)**: High-match roles (e.g., Stripe Lead Java Backend Engineer)
   - **Dropped (< 80%)**: Mismatched roles (e.g., Vercel Frontend Engineer)

### Step 3: Test the 80% Filter Policy
1. Click **"Breakdown"** on the **Vercel** role. Notice the **DROPPED (< 80%)** tag, the archive explanation with critical missing gaps, and that the "Proceed to Resume Refinement" button is hidden.
2. Click **"Breakdown"** on the **Stripe** role. Notice the **QUALIFIED (≥ 80%)** tag, category scores, and the green **"Proceed to Resume Refinement"** button.

### Step 4: Side-by-Side Diff Studio
1. Click **"Proceed to Resume Refinement"** (or **"Tailor Resume"**).
2. Review the side-by-side comparison:
   - **Left Pane**: Original base resume.
   - **Right Pane**: Tailored resume with Google XYZ formatted bullets and injected ATS keywords (`+Java 21`, `+Distributed Transactions`).
3. Click the checkmark/cross toggle on any bullet to accept or revert a rewrite.

### Step 5: Export Your Resume
1. Click **"Export Clean PDF"** to download an ATS-compliant PDF.
2. Click **"Export Word (DOCX)"** to download an ATS-compliant Word document.
