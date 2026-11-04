-- Assessments are append-only; original generations and imported UML are preserved.
ALTER TABLE tddt4iots_research.ai_comparison_run ADD COLUMN IF NOT EXISTS automatic_metrics JSONB;
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_reference (
 id UUID PRIMARY KEY, case_id BIGINT NOT NULL REFERENCES tddt4iots_research.ai_comparison_case(id),
 version VARCHAR(100) NOT NULL, approved_by BIGINT NOT NULL, provenance TEXT NOT NULL,
 inventory JSONB NOT NULL, sha256 CHAR(64) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(case_id,version)
);
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_assignment (
 run_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_run(id), reviewer_id BIGINT NOT NULL,
 assigned_by BIGINT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(run_id,reviewer_id)
);
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_evaluation (
 id UUID PRIMARY KEY, run_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_run(id),
 reference_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_reference(id), evaluator_id BIGINT NOT NULL,
 evaluator_version VARCHAR(60) NOT NULL, correspondence JSONB NOT NULL, results JSONB NOT NULL,
 rationale TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_review (
 id UUID PRIMARY KEY, run_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_run(id), reviewer_id BIGINT NOT NULL,
 round INTEGER NOT NULL CHECK(round>0), rubric_version VARCHAR(60) NOT NULL,
 scores JSONB NOT NULL, rationale TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(run_id,reviewer_id,round)
);
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_effort (
 id UUID PRIMARY KEY, run_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_run(id), reviewer_id BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL CHECK(status IN ('ACTIVE','PAUSED','COMPLETE')),
 active_since TIMESTAMPTZ, active_ms BIGINT NOT NULL DEFAULT 0 CHECK(active_ms>=0),
 baseline JSONB NOT NULL, corrected_workspace JSONB, change_log JSONB, change_summary JSONB,
 started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, finished_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX IF NOT EXISTS ix_comparison_open_effort ON tddt4iots_research.ai_comparison_effort(run_id,reviewer_id) WHERE status<>'COMPLETE';
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_comparison_cost (
 id UUID PRIMARY KEY, run_id UUID NOT NULL REFERENCES tddt4iots_research.ai_comparison_run(id), calculator_id BIGINT NOT NULL,
 tariff JSONB NOT NULL, result JSONB NOT NULL, amount_usd NUMERIC(22,10),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
