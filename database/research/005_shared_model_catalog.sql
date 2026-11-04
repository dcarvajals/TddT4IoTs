-- A directly registered base model can be selected without inventing a training job.
ALTER TABLE IF EXISTS model_use_tool ALTER COLUMN model_training_id DROP NOT NULL;

ALTER TABLE tddt4iots_research.ai_comparison_run
    ADD COLUMN IF NOT EXISTS model_registration_kind VARCHAR(16) NOT NULL DEFAULT 'TRAINED_MODEL'
    CHECK (model_registration_kind IN ('BASE_MODEL', 'TRAINED_MODEL'));

DO $$ DECLARE constraint_name TEXT;
BEGIN
    FOR constraint_name IN SELECT conname FROM pg_constraint
        WHERE conrelid = 'tddt4iots_research.ai_comparison_run'::regclass
          AND contype = 'u'
          AND pg_get_constraintdef(oid) = 'UNIQUE (case_id, model_registration_id, repetition, attempt)'
    LOOP
        EXECUTE format('ALTER TABLE tddt4iots_research.ai_comparison_run DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS ux_comparison_registration_attempt
    ON tddt4iots_research.ai_comparison_run(case_id, model_registration_kind, model_registration_id, repetition, attempt);
