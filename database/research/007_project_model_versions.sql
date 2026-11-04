-- Immutable artifacts within one CASE project. Corrections are not new generations.
CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_project_version (
 id UUID PRIMARY KEY,
 researcher_id BIGINT NOT NULL,
 project_id VARCHAR(512) NOT NULL,
 version_number BIGINT NOT NULL CHECK(version_number>0),
 parent_id UUID REFERENCES tddt4iots_research.ai_project_version(id),
 run_id UUID REFERENCES tddt4iots_research.ai_comparison_run(id),
 kind VARCHAR(16) NOT NULL CHECK(kind IN ('GENERATED','CORRECTION','MANUAL')),
 label VARCHAR(180) NOT NULL,
 notes TEXT NOT NULL DEFAULT '',
 source_workspace JSONB,
 snapshot JSONB,
 metadata JSONB NOT NULL,
 import_status VARCHAR(24) NOT NULL DEFAULT 'PENDING'
   CHECK(import_status IN ('PENDING','IMPORTED','VALIDATION_FAILED','IMPORT_FAILED','CAPTURE_FAILED','DECLINED','SAVED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(researcher_id,project_id,version_number)
);
CREATE INDEX IF NOT EXISTS ix_project_version_history ON tddt4iots_research.ai_project_version(researcher_id,project_id,created_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_project_generation_run ON tddt4iots_research.ai_project_version(run_id) WHERE kind='GENERATED' AND run_id IS NOT NULL;
-- Preserve existing research generations and their original UML on migration.
INSERT INTO tddt4iots_research.ai_project_version(id,researcher_id,project_id,version_number,run_id,kind,label,source_workspace,snapshot,metadata,import_status,created_at)
 SELECT r.id,r.researcher_id,r.project_id,ROW_NUMBER() OVER(PARTITION BY r.researcher_id,r.project_id ORDER BY r.started_at,r.id),r.id,'GENERATED',LEFT(COALESCE(r.returned_model,r.requested_model)||' · R'||r.repetition||' / A'||r.attempt,180),
 r.parsed_workspace,r.original_imported_workspace,
 jsonb_build_object('requestedModel',r.requested_model,'returnedModel',r.returned_model,
 'registrationId',r.model_registration_id,'registrationKind',r.model_registration_kind,'ownerId',r.model_owner_id,'inputSha256',c.input_sha256,
 'requirementsVersion',c.requirements_version,'systemCode',c.system_code,'phase',c.phase,
 'repetition',r.repetition,'attempt',r.attempt,'parameters',r.request_evidence,'latencyMs',r.latency_ms,'usage',r.usage,
 'promptVersion',r.prompt_version,'schemaVersion',r.schema_version,'sourceCommit',r.source_commit),
 COALESCE(r.client_status,'PENDING'),r.started_at
 FROM tddt4iots_research.ai_comparison_run r JOIN tddt4iots_research.ai_comparison_case c ON c.id=r.case_id
 WHERE r.status='SERVER_VALIDATED' AND r.parsed_workspace IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM tddt4iots_research.ai_project_version v WHERE v.researcher_id=r.researcher_id AND v.project_id=r.project_id)
 ON CONFLICT DO NOTHING;
