# Quantitative and expert assessment of comparative UML runs

## Activation and workflow

Apply `database/research/006_comparison_assessments.sql` after migrations 004 and 005; rebuild/restart Java, update the web assets and restart Python to capture provider service tier on new responses. This migration adds tables/columns, without rewriting existing generations. It has not been applied to the application database by this implementation task.

In **Requirements AI**, enable study recording, supply system/requirements version/model/repetition/attempt and generate/import the UML. Select **Load generation history → Assess**. Six numbered sections cover structural metrics, coherence checks, reference correspondences, expert review, correction effort and cost. Original request settings, tokens, time and failed attempts remain available in the existing generation CSV. **Export assessments CSV** exports measurement rows across the owner's projects; JSON exports preserve the full decisions and provenance.

Repetitions are fresh generations. Retries remain separate attempts; neither successful imports alone nor repeated tariff estimates are independent model generations. Preserve failed attempts in denominators and report the selected success definition (server-valid, locally valid or imported). Do not silently discard failures or choose the best repetition. Freeze tool/prompt/configuration after the pilot, before collecting the evaluation corpus.

## Automatic structural measurements

The server stores `automatic_metrics` when a valid provider workspace is received, and replaces that preliminary measurement with the initial imported artifact measurement on the one-time import report. New import snapshots contain an explicit inventory captured from the actual canvas data. Class counts come from the consolidated class model, not guessed regular expressions in the requirements. Missing class evidence is unavailable, not zero. Failed outputs without an artifact do not receive a fabricated inventory.

Inventory kinds: `actor`, `use_case`, `uc_relation`, `class`, `class_relation`, `attribute`, `operation`, `participant`, `message`, `response`. Actor occurrences are counted per diagram, not deduplicated globally by name. Classes include interfaces represented in the existing class array; enums are outside this version's class count. Participants reflect rendered lifelines; duplicate names in one sequence share the lifeline as in the renderer. `message` counts initiating arrows, `response` counts return arrows and `message_total` is their sum. Positions and graph layout do not affect these counts. No element count is interpreted as semantic correctness.

Each inventory element has a stable artifact-local `id`, `kind`, `label`, `owner` and, when applicable, `from`, `to`, `details`. Native slot-based message/attribute/operation IDs mean reordering can appear as multiple changes; this is why net differences are not editing-action counts. A rename can be remove+add for name-based class IDs. Inventory normalization version is `comparison-metrics-v1`.

Older snapshots can still be measured from the saved provider workspace and native class model. They do not contain the new complete inventory. New timed correction sessions require an inventory-identical baseline; use a new pilot with updated snapshots if a legacy artifact cannot reproduce that baseline. Do not substitute a new generation for an old observation under the same run ID.

## Reviewed references and correspondence metrics

Prepare the reference independently from the evaluated generation, using the frozen reviewed requirements and source use-case diagrams. A researcher registers a JSON document with:

```json
{
  "version": "reference-1",
  "provenance": "Source DOI/pages; requirement IDs; delimitations; reviewer and approval record",
  "approved": true,
  "inventory": [
    {"id":"ref-actor-1","kind":"actor","label":"Caregiver","owner":"Care management"},
    {"id":"ref-uc-1","kind":"use_case","label":"Attend emergency","owner":"Care management"},
    {"id":"ref-assoc-1","kind":"uc_relation","label":"association","owner":"Care management","from":"ref-actor-1","to":"ref-uc-1"}
  ]
}
```

The example is illustrative, not a ready reference for any of the five systems. The UI's format download contains the generated inventory with `approved=false`; it is only a format aid and must not be approved unchanged as an independent reference. Include all assessed views and granularities, using consistent scope, relation direction, cardinalities and operation signatures in `details` when relevant. State explicitly which kinds are in scope before evaluation. Version and case are immutable; reference records include creator, timestamp, provenance and SHA-256. References belong to one frozen requirements case and can be reused across its model conditions.

Select the reference and explicitly pair each generated item with an equivalent item of the same kind. A reference item may be used once. The reviewer must confirm all matches and unmatched items and provide a rationale. For edges/messages, review direction, endpoints, owning use case and intended meaning; a similar label alone is insufficient. This is a recorded expert correspondence assessment, not an automatic semantic oracle. Preserve disagreements as separate assessments and adjudicate them outside the independent rounds.

Per kind and micro-aggregated across inventory elements:

- TP = approved one-to-one pairs.
- FP = generated count − TP.
- FN = reference count − TP.
- Precision = TP/(TP+FP).
- Recall = TP/(TP+FN).
- F1 = 2TP/(2TP+FP+FN).

A zero denominator produces `null`/N/A, never an invented perfect score. F1 is zero when items are missed or spurious and there are no true positives. Both inventories empty produces N/A. Micro F1 mixes nodes/edges/granularities and must be accompanied by per-kind results; it is not a stand-alone claim of overall quality. All assessments are append-only with reviewer, reference version, complete correspondence list, rationale and evaluator version.

## Coherence checks

Deterministic structural checks cover use-case/sequence presence, valid sequence message endpoints, valid flow actors, actor–use-case associations and use-case relation endpoints. These produce PASS/FAIL per checked occurrence.

Cross-view class/lifeline and message/receiver-operation name checks use normalization of case, accents and punctuation. They produce MATCH_CANDIDATE, UNMAPPED or NOT_ASSESSED, not semantic PASS/FAIL. They cannot resolve synonyms, abstraction differences or natural-language action descriptions. Expert rubric scores and correspondence decisions provide the semantic assessment. Retain unmapped candidates for manual analysis rather than silently counting them as errors or matches. This implementation is a defined rule suite, not a proof of complete UML/OCL conformance.

## Expert review and access

The owner can grant access using an existing application user ID. Reviewers find **Load runs assigned to me for review**. Assignment permits reading this run's UML and assessments, creating correspondence assessments, reviews and personal correction sessions. It does not permit editing the owner's reference or tariff, reading unrelated runs or obtaining provider credentials. It can expose the same reference and previous ratings: the UI is not a blinded study platform. Record reviewer expertise, affiliation/conflicts, calibration procedure and whether authors/tutors reviewed their own artifact in the study protocol.

Rubric `expert-uml-v1`, five criteria:

| Criterion | Evidence assessed |
|---|---|
| Correctness | Models express the intended behavior and constraints. |
| Completeness | In-scope reviewed requirements are covered. |
| Cross-view coherence | Actors, classes, interactions and constraints agree across views. |
| Traceability | Elements can be justified by identifiable requirements/source passages. |
| Readability | Names and diagrams support an understandable interpretation. |

Ordinal anchors: 1 major deficiencies; 2 substantial corrections; 3 partially satisfactory; 4 minor corrections; 5 satisfies the criterion. Require a rationale with concrete evidence/requirement IDs. Store separate reviewer/round/criterion scores; no automatic conversion to F1. A reviewer/round cannot be overwritten; corrections require a new round. Calibrate this rubric during the pilot and document disagreement/adjudication and agreement statistics in the analysis. Do not claim inter-rater reliability from one reviewer or from uncalibrated self-assessment.

## Human correction effort

Restore the initial UML before starting. The server checks that the current inventory equals the immutable initial inventory. Start, pause, resume and finish are persisted with server timestamps. Only ACTIVE intervals contribute to `active_ms`; pauses and browser refreshes do not silently end a session. An active session continues until explicitly paused/finished, so pause during interruptions. Closing a browser is not evidence of working time.

Correction actions are manually logged as add/remove/modify/layout with descriptions; each log action is persisted immediately and survives reopening the panel. Finish captures the corrected workspace separately from the original. The server computes added/removed/modified inventory items and retains the change log. Net artifact difference and documented action count are separate variables; neither is claimed to capture every click or subjective cognitive effort. Multiple correction sessions remain separate records; summing sessions against the same initial baseline requires an explicit analysis decision.

Save the CASE project normally to retain ongoing edits. The assessment JSON also preserves completed correction snapshots. Do not start the timer after making unrecorded corrections. The owner and assigned reviewers can inspect the original, but only a session's authenticated reviewer can control that session.

## Estimated monetary cost

Enter the applicable model's USD per-million-token rates, pricing source URL, effective date, service tier, context tier and processing region. The model must match the returned provider model (requested model only if returned ID is unavailable); confirm rates apply to that exact condition. When the provider returns `service_tier`, the server checks the declared tier against it. Rates are not embedded as permanently current model prices. They must be reviewed against the applicable official pricing or contract for the run date.

Let I be prompt tokens, C cached-read tokens, W cache-write tokens, O completion tokens and p the applicable rates. Estimated cost = ((I−C−W)×p_input + C×p_cached + W×p_write + O×p_output)/1,000,000. If 12-hour cache-write usage is reported, its rate is required and calculated separately. Reasoning tokens are already included in O and are not added again. Missing usage produces USAGE_UNAVAILABLE and null amount, including timed-out attempts whose invoice impact cannot be inferred. Impossible token counts and negative/missing rates are rejected. No automatic provider request is made to calculate cost.

Amounts are estimates, not reconciled invoices: taxes, tools, infrastructure and unrelated requests are excluded. Long-context multipliers, regional uplifts and service-tier prices must already be reflected in the explicitly confirmed rates. Multiple tariff estimates for a run are append-only revisions, not multiple costs to sum. Select a declared estimate/version per run when aggregating. Original failures must remain in overall performance summaries even if their cost is unknown.

Official references consulted for this implementation: [OpenAI pricing](https://developers.openai.com/api/docs/pricing), [prompt caching](https://developers.openai.com/api/docs/guides/prompt-caching), [Chat Completions usage](https://developers.openai.com/api/reference/resources/chat/subresources/completions/methods/create).

## Export and verification

Generation CSV remains one row per run with system, model, repetition, attempt, status, latency and tokens. Assessment CSV is long-format: one row per measurement and assessment ID, with reviewer, metric name/value and TP/FP/FN where applicable. It includes all correspondence assessments, expert rounds, completed correction sessions and cost versions, not a silently selected latest result. N/A remains blank; don't replace it with zero. Join using `run_id`; use JSON for full reference versions, tariff, rubric, decisions and corrected artifacts. Totals must not double count repeated assessments or tariff revisions.

No production data, real references, expert scores or paid generations are fabricated by installation. Automated tests use isolated `comparison_test` PostgreSQL plus offline deterministic fixtures.

### Automatic correction audit

During an active **Correction effort** session, the UML page now observes committed pointer/keyboard/form/drop interactions and checks the workspace every 1.5 seconds for asynchronous changes. The **Corrections** toolbar button reopens the timer and log. The observer stores successive workspace differences (path, before and after values), a client event UUID, trigger and capture time. The server adds receipt time and associates the event with the authenticated reviewer, run and correction session. Original generated evidence is unchanged. Content and serialized graph changes, including layout, are captured; tab selection and derived inventory are excluded. A transition can contain multiple changes. This is an audit of observed states, not a complete command/keystroke history: several synchronous mutations in one interaction are grouped, and transient changes between observation points may not be seen. Do not describe the event count as the exact number of user commands.

Pending events are queued in browser local storage and sent in batches. Lost acknowledgements are retried with the same UUIDs and deduplicated within the session. The panel displays pending/error status, and leaving with pending events triggers a browser warning. Recovery is limited to this browser/origin; do not clear browser storage before synchronization. Pending events must be saved before pausing/completing or switching sessions. If storage is unavailable, the page displays an error and must remain open until synchronization. The log records observed content, potentially including requirements details; exports should follow the study's data handling rules.

Capture operates only while the selected run's correction session is ACTIVE and its workspace is loaded. Paused edits are not added to the automatic event count; resuming establishes a new observation baseline. For comparable effort measurements, participants must not edit while paused. The final inventory difference can still include unmeasured paused edits. Switching the loaded version or starting another generation requires pausing or finishing first. Reloading does not reconstruct unsaved canvas edits; the recovered event queue is evidence of observed transitions, not an autosave/replay feature.

Manual annotations remain available to explain reasons. Exports retain `source=automatic|manual`, `capturedAt` (client), `receivedAt` (server), patches and event IDs. Completion summaries distinguish `automaticEvents`, `manualAnnotations`, `observedFieldChanges` (patch count) and the existing net inventory difference. An edit followed by its reversal produces two observed events, even when the final inventory has no net difference. Counts should not mix manual annotations with automatic events as independent corrections. Automatic batches are limited to 20 events, 1,000 patches / 2,000,000 serialized characters per event and 10,000 total events per session; rejected events remain pending with an error rather than being silently discarded.

Deployment: update the Java assessment service and web resources (including `correction-audit.js`), then hard-refresh the UML page. No additional SQL migration is required beyond the existing research migrations. Automatic capture applies to registered study runs with an initial imported workspace; it does not retroactively recover edits made before capture was started.
