# Project versions and model comparisons

## Activate

Apply `database/research/007_project_model_versions.sql` after 004–006, rebuild the shared DTO and OpenAI service, restart Java and publish the updated web JS/HTML/CSS. This migration has only been executed on the isolated test database during implementation, not the application database. No Python changes are required for this feature.

## Interface

The existing project toolbar shows **AI model** and **Versions & comparisons**. AI model opens a dedicated modal, keeping the original three-area UML header layout. The modal groups personal and shared model registrations, identifies their owner and registration number and disables unavailable entries. Requirements AI and the versions window show a read-only model summary with a button opening that same modal. Apply to this project commits the draft choice; Cancel leaves it unchanged and returns to the originating window when applicable. An empty choice explicitly uses the user's Settings selection; a chosen project registration is resolved server-side on every request using its owner's credential. It does not mutate Settings or silently substitute a different model.

**Save all** retains the project selection and loaded version lineage in the usual CASE workspace. Existing projects without those fields still load. The selection is not automatically changed when an old version is opened: the header separates the working origin from the model for the next generation. Legacy versions cannot retrospectively identify an unknown model; manual snapshots are labeled accordingly.

The versions window provides model/type/text filters, creation date/status, original model registration, requirements version, repetition/attempt and actions to open, download JSON and assess the source research run. Version numbers are allocated sequentially per user/project under a PostgreSQL transaction advisory lock and retained permanently. Corrections and manual snapshots use the same project sequence; generation repetitions/attempts remain separate metadata.

Every completed, server-validated document-based generation with a project ID is archived before its response returns to the browser, even without study recording. The raw generated workspace and actual model/settings/input hash are preserved server-side. The initial native canvas snapshot is attached once after import. A response rejected by the browser or declined by the user remains in history with that status. An original without an initial snapshot may be reopened from the generated JSON, passing local validation again. A pending first import can then capture its initial snapshot; an earlier recorded decline/validation failure is not silently rewritten.

Study recording remains necessary for full input documents, failure evidence, frozen experiment cases, expert assessment and quantitative exports. Failed recorded attempts appear in the same history without a loadable version and cannot be selected as complete artifacts. An unrecorded failed request is not turned into a fabricated version or research observation.

**Save new version** captures the current native use-case/class/sequence data and graphs, inventory, active diagram and test-model data. If an archived origin is loaded, the new version is a CORRECTION with an immutable parent and inherited source model/run. Without an archived origin, it is MANUAL and no model is attributed. Saving corrections does not create new model calls, repetitions, attempts or token costs. Save all still updates the normal working copy; it does not rewrite an archived snapshot. Manual JSON imports clear inherited model/run lineage.

Opening an archived entry asks before replacing the current canvas, restores that entry's diagrams and sets the working-origin pointer. The source JSON and initial snapshot remain unchanged. An active working copy may differ from its origin after edits; the header therefore says "Working from", not that it is still byte-identical to the archived version. Save new version to retain those edits in this archive.

## Comparison

Select two to four entries and choose **Compare selected**. The view shows model, type/status, requirements SHA-256, parameters, generation time/tokens and structural counts. Counts come from the saved artifact; missing imported class evidence is N/A. Corrections do not display inherited generation time/tokens as a second paid/model execution.

Warnings identify different/unknown requirement inputs, settings/prompt/schema/revision/phase and derived artifacts. A missing `GIT_COMMIT` source revision is explicitly unverified. These are descriptive comparisons: counts and successful imports do not establish semantic correctness. Use **Assess source run** for reviewed references, correspondence-based F1, expert review, correction effort and costs of the original research generation. That action evaluates the source run, not a silently substituted corrected artifact.

## Storage and compatibility

`ai_project_version` stores UUID, researcher/project, stable project number, immutable parent, optional research run, entry type, label/notes, source workspace, native snapshot, actual generation metadata, import status and creation time. Initial import reports are one-time updates; saved corrections always create a new row. Access is scoped to the authenticated researcher. Existing reviewer assignments continue to grant access to the source research run through the assessment workflow, not all archived project versions.

Migration 007 imports existing server-validated research generations and their initial UML without changing those runs. It is idempotent. CASE files predating generation evidence cannot be assigned an actual historical model and can instead be archived as manual snapshots. Native snapshots from older deployments may have less metadata; the UI does not invent missing values.

## Verification

Backend tests cover explicit project selection without global-selection writes, owner credential resolution, ordinary generation archival, exact model metadata, immutable roots/corrections, independent project numbering, user/project boundaries and idempotent migration/backfill. Offline UI behavior tests run with:

```sh
node --test Tddt4iotsMaven/src/test/js/project-versions.test.cjs
```

These simulate the legacy controller's model selection, filters, comparison warnings and version saves without paid requests. Browser rendering and a real native UML round trip should also be checked in the deployed application after activation; these offline tests do not prove visual layout or replace a pilot.
