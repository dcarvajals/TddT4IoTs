# AI Experiment Studio

`001_ai_experiment_studio.sql` is an additive PostgreSQL schema for the research workflow. It intentionally keeps the operational `training_history` table intact.

Apply the migrations in order:

1. `001_ai_experiment_studio.sql`
2. `002_training_monitor.sql`
3. `003_research_runtime_integration.sql`
4. `004_comparative_generations.sql` — opt-in evidence for the current comparison of models across separate CASE projects.
5. `005_shared_model_catalog.sql` — direct base-model selections without training jobs and distinct base/trained registration keys in comparison evidence.

The current article evaluates generations from existing models. The comparative workflow is independent of training jobs and is documented in [the study runbook](../../docs/comparative-study-runbook.md). The original training-oriented workflow and experiment matrix below are retained for other research activities; fine-tuning and ablations are not required for this study.

The third migration connects the operational records with the scientific schema. After the OpenAI microservice restarts, an automatic backfill imports existing training histories and synchronizes stored checkpoints. New reviewed datasets, training jobs and CASE workspace generations are recorded at their normal application entry points.

Authenticated research endpoints:

- `GET /research/summary`: counts, training runs and aggregated metrics.
- `GET /research/review-items`: dataset samples available for expert review.
- `POST /research/human-review`: rubric scores, correction effort and decision.
- `POST /research/backfill`: manually trigger the idempotent historical import.
- `GET /research/export-training.csv`: publication-ready training run export.

## Reproducible workflow

1. Create a dataset version and load one approved, anonymized project per `ai_dataset_item`.
2. Assign the split by project (`TRAIN`, `VALIDATION`, `TEST`) before experimentation. Never place variants of one project in different splits.
3. Freeze the dataset manifest and its SHA-256 hash.
4. Register prompt, workspace schema, source commit, model snapshot, parameters and seed in `ai_experiment`.
5. Store every generation in `ai_inference_run`, including invalid outputs and retries.
6. Store atomic measurements in `ai_metric_result`; do not store only averages.
7. Record expert review and correction effort in `ai_human_review`.
8. Export the two summary views for analysis, while using per-run rows for paired statistical tests.

## Initial experiment matrix

- `E0`: current implementation baseline.
- `E1`: base model and simple prompt.
- `E2`: examples plus strict JSON Schema.
- `E3`: requirements extraction, UML synthesis and deterministic validation.
- `E4`: E3 with supervised fine-tuning, only when the account/provider supports it.
- `E5`: ablations without schema, examples, stages or validators.

Recommended metric groups are `STRUCTURE`, `USE_CASE`, `CLASS`, `SEQUENCE`, `CONSISTENCY`, `OPERATIONAL` and `HUMAN`.

Apply `006_comparison_assessments.sql` after 004/005 before deploying the updated generation service. It adds automatic metrics, immutable reviewed references, correspondence assessments, expert rounds, reviewer assignments, correction sessions and tariff-based cost estimates. See [assessment workflow](../../docs/research/comparison-assessments.md). No production migration is executed automatically.

Apply `007_project_model_versions.sql` after 006 to enable immutable project history and corrections. Existing validated research outputs are backfilled. See [project versions](../../docs/research/project-versions.md). Rebuild shared DTOs and Java, then update web assets.
