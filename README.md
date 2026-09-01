# 🚀 SmartResume Tailor
### AI-Powered Job Matcher and Resume Optimization System
**Tech Stack:** Java 21 • Spring Boot 3.3.4 • PostgreSQL 16 + pgvector • React 19 + TypeScript + Vite + Tailwind CSS

---

## 📖 Overview
SmartResume Tailor automates the entire job search and resume alignment cycle:
1. **Ingestion & Parsing**: Extracts text from PDF/DOCX resumes losslessly using Apache Tika, converting it into structured JSON.
2. **Job Search & Ingestion**: Fetches unstructured job descriptions and extracts structured requirements.
3. **Deterministic Semantic Matching**: Computes a 0–100% quantitative score across:
   - **Skills Alignment**: Max 35 pts
   - **Experience & Seniority**: Max 30 pts
   - **Domain & Architecture Alignment**: Max 20 pts
   - **Education & Certifications**: Max 15 pts
4. **Strict 80% Cutoff Policy**:
   - **Score < 80%**: Marked `DROPPED_LOW_MATCH` and archived with explicit gap explanation.
   - **Score ≥ 80%**: Marked `QUALIFIED` and eligible for refinement.
5. **Zero-Hallucination Refinement**:
   - Applies Google's XYZ formula ("Accomplished [X], as measured by [Y], by doing [Z]").
   - Injects domain-specific ATS keywords without fabricating experience.
   - Computes granular diff items comparing original vs. tailored bullet points.
6. **ATS-Compliant Document Export**: One-click download as clean single-column PDF (OpenPDF) or Word document (Apache POI).

---

## 🏗️ Project Architecture

```
SmartResume_Tailor/
├── smartresume-api/                     # Spring Boot 3.3.4 Application (Java 21)
│   ├── src/main/java/com/smartresume/tailor/
│   │   ├── config/                      # CORS, Async, and AI Bean configurations
│   │   ├── domain/                      # Entities, DTOs, and Enums
│   │   ├── ingestion/                   # ResumeParserService (Tika), JobIngestionService & Controllers
│   │   ├── matcher/                     # SemanticMatcherService & MatchController
│   │   ├── refinement/                  # ResumeRefinementService & TailorController
│   │   └── export/                      # DocumentExportService & ExportController
│   └── src/test/java/com/smartresume/tailor/
│       ├── ingestion/                   # ResumeParserServiceTest, JobIngestionServiceTest, Controller tests
│       ├── matcher/                     # SemanticMatcherServiceTest, MatchControllerTest
│       ├── refinement/                  # ResumeRefinementServiceTest, TailorControllerTest
│       ├── export/                      # DocumentExportServiceTest, ExportControllerTest
│       ├── regression/                  # SmartResumeRegressionE2ETest (Full lifecycle E2E)
│       └── repository/                  # In-memory test repository doubles
├── smartresume-ui/                      # React 19 + TypeScript + Vite UI
│   ├── src/
│   │   ├── components/                  # Domain-packaged UI components
│   │   │   ├── dashboard/               # MetricCards, JobSearchControls, JobPipelineTable
│   │   │   ├── matching/                # ScoreGauge, MatchBreakdownModal
│   │   │   └── tailor/                  # SideBySideDiffEditor
│   │   ├── test/                        # Mirrored test structure & automation suite
│   │   │   ├── dashboard/               # MetricCards.test.tsx, JobSearchControls.test.tsx, JobPipelineTable.test.tsx
│   │   │   ├── matching/                # ScoreGauge.test.tsx, MatchBreakdownModal.test.tsx
│   │   │   ├── tailor/                  # SideBySideDiffEditor.test.tsx
│   │   │   ├── automation/              # FrontendAppAutomation.test.tsx, UserWorkflowRegression.test.tsx
│   │   │   └── setup.ts                 # Vitest testing environment configuration
│   │   ├── services/                    # API client connecting to Spring Boot endpoints
│   │   └── types/                       # TypeScript interfaces
├── docker-compose.yml                   # PostgreSQL 16 + pgvector container definition
├── init-db.sql                          # Database DDL with HNSW vector index creation
└── smart_resume_tailor_blueprint.md     # Full architectural specification document
```

---

## ⚡ Quick Start

### 1. Start Vector Database (Optional — H2 in-memory is default)
To use PostgreSQL 16 with the `pgvector` extension:
```bash
docker compose up -d
```

### 2. Run Spring Boot Backend
```bash
cd smartresume-api
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`.

### 3. Run Frontend UI
```bash
cd smartresume-ui
npm install
npm run dev
```
The application will be available at `http://localhost:5173`.

---

## 🧪 Running Automated Tests
```bash
cd smartresume-api
mvn test
```
All unit tests validate:
- Candidate qualification for roles meeting $\ge 80\%$ threshold.
- Immediate archiving of roles falling below $< 80\%$.
- Prohibition of refinement on low-match roles.
- Generation of valid ATS-parseable PDF and DOCX binary streams.
