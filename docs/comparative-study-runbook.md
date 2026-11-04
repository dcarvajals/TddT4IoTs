# Comparative UML study: first implementation milestone

## Scope

Use a CASE project per IoT system and archive model conditions in **Versions & comparisons**; separate projects remain supported. See [project history and explicit model selection](research/project-versions.md). Existing encrypted project references are preserved as opaque identifiers, not converted into numeric IDs. The shared model's registration determines the provider credential internally. Each researcher owns their generation records; this milestone does not grant other users access to their evidence.

The comparison workflow now includes structural counts, a defined coherence rule suite, reviewed references and explicit correspondence-based precision/recall/F1, expert rubric rounds, timed correction sessions and versioned token-cost estimates. See [assessment workflow and formulas](research/comparison-assessments.md). Real references, expert judgments and tariff confirmation must be supplied by the researchers. Automatic matrix execution and study-specific statistical analysis remain separate work. Counts and successful imports must not be reported as semantic quality.

Prompt `case-workspace-v2` exposes editor constraints to the model, including 1-based alternative-flow indices. For a use case with N normal steps, `1 <= startStep <= N`; a returning alternative must satisfy `startStep <= returnStep <= N`, while a terminating alternative requires `returnStep=null`. Java now enforces these alternative-flow rules before marking a generation server-validated, and the browser retains its import validation and local class-parser checks. Python's schema includes descriptions of these cross-field constraints; descriptions guide generation but do not guarantee semantic compliance. Invalid generations remain failed observations with their original output. No repair request or silent index correction is performed. Returning to an earlier step is an editor limitation, not a general UML prohibition; do not reinterpret the requirements simply to pass validation. Earlier runs retain their original prompt version.

Generation settings are available even when study recording is disabled. Complete-workspace generation starts with a 32,000-token output budget; this includes internal reasoning as well as the visible JSON. For an Astra pilot, explicitly select Low reasoning to test a smaller reasoning budget, rather than relying on the provider default. This is a pilot configuration, not a validated quality setting. `finish_reason=length` is always retained as a failed, incomplete observation and never imported, even when its text happens to parse. The modal displays generation errors at the top. Fix the configuration after the pilot before executing comparisons; do not mix changed limits or reasoning levels silently in the same evaluated condition.

## Activation

1. Apply research migrations 001–003 if they have not already been installed, then apply `database/research/004_comparative_generations.sql`, `005_shared_model_catalog.sql`, `006_comparison_assessments.sql` and `007_project_model_versions.sql` to the application's PostgreSQL database. Migration 004 is additive and leaves training history intact. Migration 005 allows selections without a training job and distinguishes base and trained registration IDs in comparative evidence.
2. Rebuild the shared Java DTO module, repository dependency and OpenAI microservice together, then restart the microservice and Python gRPC service and publish the updated web assets. The repository dependency declares a snapshot DTO version; align it with the reactor version when building, e.g. `-Dversion.ms-core-tddt4iots=1.0.0`.
3. Set `GIT_COMMIT` on the microservice to the exact tested source revision. `local-development` in an export means that a source revision was not supplied and is insufficient to identify the evaluated build by itself.
4. Verify access to the registered model using separate pilot material. This delivery does not claim live access or successful generation with any particular OpenAI model.

Ordinary generation remains available with research recording switched off. Research generation fails before calling the provider if the new schema is unavailable; do not enable recording until migration 004 has been applied.

The Java gRPC deadline is configured by `grpc.deadline.after.timeout` (660 seconds by default), longer than Python's 600-second HTTP timeout. Restart Java after changing this setting and check for deployment overrides. A deadline failure does not prove that the provider stopped processing the request; retries remain explicit, separately numbered attempts. Training monitoring skips records whose owners have no saved credential and checks them again on the next cycle, without sending invalid provider requests or counting these skips as provider failures.

For `Request timed out` errors, inspect Python's `Diagnóstico de generación` line: it includes the requested model, input character count, output token limit, elapsed milliseconds, SDK exception type, transport cause type and provider request ID when available. It excludes credentials and requirement text. Python sets the 600-second timeout explicitly on the OpenAI SDK as well as the HTTP client. A timeout alone does not establish a model-access, latency or networking cause; compare the selected identifier, transport cause and a separate small pilot before changing the evaluated configuration. These diagnostics are also retained inside the provider envelope in recorded comparison runs. There are no automatic retries or automatic model substitutions.

## Running a case

1. Open the user menu → **Settings → Available models**. The dialog opens on that tab. **My registered models** and **Models registered by other users** show base registrations and trained versions separately by row type. Each row includes owner, exact provider identifier, registration/version and availability. Choose **Use this model**; **Current model** and **In use** confirm the selection. A directly registered base model requires no training job. Its registered name must be the provider's actual model identifier. Availability is based on saved registration and credentials, not a paid provider access check. Then open the CASE project for that system/model condition. The selection is per user and applies to subsequent generations across their projects; existing diagrams and saved versions are not regenerated.
2. Open **Generate the complete CASE model** and enable **Record this generation for the comparative study**.
3. Enter study code, phase, system ID, requirements version, repetition and attempt. Use `PILOT` with separate pilot material; use `EVALUATION` for the frozen evaluation corpus. Phase separation does not itself prevent reusing pilot material: researchers must enforce that separation when selecting documents.
4. Supply the approved natural-language specification. Expert approval is external to this milestone; recording a case does not certify expert review. Do not upload reference UML or full source papers as model inputs if they would disclose the expected output.
5. Keep the effective input identical across model projects: the combined input includes the description, document names, document order and extracted text. The first run fixes its SHA-256 for each researcher/study/phase/system/requirements-version combination. Changed input requires a new version or study code. This guard prevents accidental differences; it is not a revision approval workflow.
6. Set compatible output-token, reasoning and optional temperature settings during the pilot, then keep the evaluation protocol fixed. Omitted temperature/reasoning means provider default; that is explicitly recorded. No parameter is silently substituted after a provider error. Different settings across runs remain visible in the JSON evidence; the application does not yet freeze the whole experimental protocol.
7. Generate and review the import result. The server stores a run before the provider call. Duplicate model-registration/repetition/attempt combinations are rejected before another paid request. Increment repetition for an independent generation. Increment attempt for a retry of the same repetition; retain earlier failures in the analysis.
8. Save the project using its normal modeling controls. Study settings and the generation UUID are saved with the workspace, so saved versions retain their link to the immutable evidence. Later manual edits do not replace the original evidence snapshot.

## Evidence and outcomes

Every recorded attempt includes the executing researcher, project, model registration and owner, requested/returned model, exact messages, configuration, prompt/schema versions, source revision, original documents, effective-input hash, original model output, provider envelope, tokens and latency when available. Credentials are excluded from evidence. The Python adapter makes one provider request per attempt (`max_retries=0`, timeout 600 seconds), using a fresh messages list with no previous conversation.

- `STARTED`: no terminal result has been saved, including a possible process interruption. It is not a successful or automatically failed generation.
- `SERVER_VALIDATED`: the server accepted the response structurally; this does not imply successful client import or semantic fidelity.
- `FAILED`: provider/transport error, malformed output, failed server validation or token truncation. The available raw output is retained.
- `REFUSED`: the provider returned a refusal.

The client reports a separate one-time outcome: `IMPORTED`, `VALIDATION_FAILED`, `IMPORT_FAILED`, `CAPTURE_FAILED` or `DECLINED`. Successful capture stores use-case data/graphs, the derived class model/graph and sequences immediately after import, before edits. These are client-reported artifacts, not an independent server reproduction of the importer. A missing report remains **Not reported**. If transmission fails, a recovery JSON is downloaded; retain it with the experiment. It can be submitted to the same import-report endpoint using the retained `status`, `validation.errors`, `validation.warnings` and `workspace` without reconstructing the snapshot after editing.

Document extraction handles DOCX paragraphs and tables. A document with no extractable text is rejected instead of silently ignored. Image-only PDFs and UML images require a reviewed transcription; no OCR or diagram interpretation is claimed.

## Access and exports

Authenticated requests use the `userToken` header. The server filters records by the authenticated researcher, including import reports and downloads.

- `GET /research/comparison-runs?projectId=...`: this researcher's runs in a project.
- `GET /research/comparison-runs/{uuid}`: full evidence JSON, including source documents and the original imported UML when available.
- `POST /research/comparison-runs/{uuid}/import`: one-time client result; completed reports cannot be overwritten.
- `GET /research/comparison-export.csv?projectId=...`: project observations, including failures and missing client reports.
- `GET /research/comparison-export.csv`: this researcher's observations across all projects, including pilots, with phase/system/model/repetition/attempt columns. Filter the intended study and phase explicitly for analysis.

Use **Load generation history**, **JSON**, **Download CSV** and **CSV · all my projects** in the generation dialog. CSV rows are observations, not independent systems or final statistical results. Latency measures the server-side provider/validation interval; it does not measure the client's import time. Provider configuration details and full token accounting are retained in the JSON envelope. No cost estimate is invented when pricing data are unavailable.

The adapter uses `max_completion_tokens` and optional compatible sampling/reasoning fields as documented in the [official Chat Completions reference](https://developers.openai.com/api/reference/python/resources/chat/subresources/completions/methods/create). Model/account compatibility still requires the pilot.

## Validation

Java tests cover owner credentials, serialization, context validation, DOCX table extraction, generation transport failures, truncation/refusal handling and raw output preservation. PostgreSQL tests additionally verify migration execution and reapplication, fixed input across projects, duplicate attempts, pilot separation, researcher isolation and one-time imported artifacts. `ComparisonEvidencePostgresTest` runs only when `comparison.test.jdbcUrl` explicitly names a disposable local database `comparison_test`; it recreates only its comparison tables before each test. Never point it at application data.

Python adapter tests use mocks and require no API key or network:

```sh
PYTHONDONTWRITEBYTECODE=1 PYTHONPATH=ms_core_tddt4iots_py python3 -m unittest openai_model.test_inference -v
```
