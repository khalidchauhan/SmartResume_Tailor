-- Enable pgvector and UUID extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS vector;

-- Drop existing tables if restarting
DROP TABLE IF EXISTS tailored_resumes CASCADE;
DROP TABLE IF EXISTS job_matches CASCADE;
DROP TABLE IF EXISTS job_postings CASCADE;
DROP TABLE IF EXISTS base_resumes CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS match_status_enum CASCADE;
DROP TYPE IF EXISTS tailor_status_enum CASCADE;

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Base Resumes Table
CREATE TABLE base_resumes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID,
    file_name VARCHAR(255) NOT NULL,
    storage_url TEXT,
    raw_text TEXT NOT NULL,
    parsed_json JSONB NOT NULL,
    embedding vector(1536),
    is_primary BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Job Postings Table
CREATE TABLE job_postings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    external_id VARCHAR(255),
    source VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    is_remote BOOLEAN DEFAULT false,
    raw_description TEXT NOT NULL,
    parsed_requirements JSONB,
    embedding vector(1536),
    salary_min NUMERIC(12, 2),
    salary_max NUMERIC(12, 2),
    currency VARCHAR(10) DEFAULT 'USD',
    posted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_external_source UNIQUE (external_id, source)
);

-- Job Matches Table
CREATE TYPE match_status_enum AS ENUM ('DROPPED_LOW_MATCH', 'QUALIFIED', 'ARCHIVED');

CREATE TABLE job_matches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resume_id UUID NOT NULL REFERENCES base_resumes(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    overall_score INT NOT NULL CHECK (overall_score >= 0 AND overall_score <= 100),
    skills_score INT NOT NULL CHECK (skills_score >= 0 AND skills_score <= 35),
    experience_score INT NOT NULL CHECK (experience_score >= 0 AND experience_score <= 30),
    domain_score INT NOT NULL CHECK (domain_score >= 0 AND domain_score <= 20),
    education_score INT NOT NULL CHECK (education_score >= 0 AND education_score <= 15),
    status match_status_enum NOT NULL,
    archive_reason TEXT,
    evaluation_json JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_resume_job_match UNIQUE (resume_id, job_id)
);

-- Tailored Resumes Table
CREATE TYPE tailor_status_enum AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');

CREATE TABLE tailored_resumes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    match_id UUID UNIQUE NOT NULL REFERENCES job_matches(id) ON DELETE CASCADE,
    tailored_json JSONB NOT NULL,
    diff_json JSONB NOT NULL,
    ats_score INT CHECK (ats_score >= 0 AND ats_score <= 100),
    status tailor_status_enum DEFAULT 'PENDING',
    pdf_export_url TEXT,
    docx_export_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes & HNSW Vector Search
CREATE INDEX idx_base_resumes_embedding_hnsw 
ON base_resumes USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

CREATE INDEX idx_job_postings_embedding_hnsw 
ON job_postings USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

CREATE INDEX idx_job_matches_score_status ON job_matches (status, overall_score DESC);
CREATE INDEX idx_job_matches_resume_id ON job_matches (resume_id);
