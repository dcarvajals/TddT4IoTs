-- Runtime integration between the operational training flow and the scientific schema.
-- Run after 001_ai_experiment_studio.sql and 002_training_monitor.sql.

CREATE TABLE IF NOT EXISTS tddt4iots_research.ai_training_link (
    training_history_id BIGINT PRIMARY KEY REFERENCES training_history(id) ON DELETE CASCADE,
    dataset_version_id  BIGINT NOT NULL REFERENCES tddt4iots_research.ai_dataset_version(id),
    experiment_id       BIGINT REFERENCES tddt4iots_research.ai_experiment(id) ON DELETE SET NULL,
    training_run_id     BIGINT REFERENCES tddt4iots_research.ai_training_run(id) ON DELETE SET NULL,
    linked_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE tddt4iots_research.ai_inference_run
    ALTER COLUMN dataset_item_id DROP NOT NULL;
ALTER TABLE tddt4iots_research.ai_inference_run
    ADD COLUMN IF NOT EXISTS training_history_id BIGINT REFERENCES training_history(id) ON DELETE SET NULL;
ALTER TABLE tddt4iots_research.ai_inference_run
    ADD COLUMN IF NOT EXISTS input_requirements JSONB;

ALTER TABLE tddt4iots_research.ai_artifact
    ADD COLUMN IF NOT EXISTS dataset_version_id BIGINT
        REFERENCES tddt4iots_research.ai_dataset_version(id) ON DELETE CASCADE;
ALTER TABLE tddt4iots_research.ai_artifact DROP CONSTRAINT IF EXISTS ai_artifact_check;
ALTER TABLE tddt4iots_research.ai_artifact DROP CONSTRAINT IF EXISTS ck_ai_artifact_owner;
ALTER TABLE tddt4iots_research.ai_artifact
    ADD CONSTRAINT ck_ai_artifact_owner CHECK (
        dataset_version_id IS NOT NULL OR experiment_id IS NOT NULL OR inference_run_id IS NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_training_run_provider_job
    ON tddt4iots_research.ai_training_run(provider_job_id)
    WHERE provider_job_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_training_checkpoint_provider
    ON tddt4iots_research.ai_training_checkpoint(provider_checkpoint_id)
    WHERE provider_checkpoint_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_ai_inference_training_history
    ON tddt4iots_research.ai_inference_run(training_history_id, executed_at DESC);

CREATE OR REPLACE VIEW tddt4iots_research.v_training_research_summary AS
SELECT h.id AS training_history_id,
       l.dataset_version_id,
       l.experiment_id,
       l.training_run_id,
       h.provider_job_id,
       h.provider_status,
       h.progress_percent,
       h.last_monitored_at,
       r.base_model,
       r.resulting_model,
       r.trained_tokens,
       r.training_loss,
       r.validation_loss,
       r.validation_token_accuracy,
       COUNT(DISTINCT c.id) AS checkpoint_count,
       COUNT(DISTINCT i.id) AS inference_count
FROM training_history h
JOIN tddt4iots_research.ai_training_link l ON l.training_history_id = h.id
LEFT JOIN tddt4iots_research.ai_training_run r ON r.id = l.training_run_id
LEFT JOIN tddt4iots_research.ai_training_checkpoint c ON c.training_run_id = r.id
LEFT JOIN tddt4iots_research.ai_inference_run i ON i.training_history_id = h.id
GROUP BY h.id, l.dataset_version_id, l.experiment_id, l.training_run_id,
         h.provider_job_id, h.provider_status, h.progress_percent, h.last_monitored_at,
         r.base_model, r.resulting_model, r.trained_tokens, r.training_loss,
         r.validation_loss, r.validation_token_accuracy;

COMMENT ON TABLE tddt4iots_research.ai_training_link IS
    'Stable bridge from the operational training history to reproducible scientific records.';
