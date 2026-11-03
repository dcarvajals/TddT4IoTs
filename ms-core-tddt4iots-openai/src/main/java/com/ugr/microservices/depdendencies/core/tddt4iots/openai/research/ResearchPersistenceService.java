package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.CreateFileTrainingReq;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ResearchDatasetSampleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResearchPersistenceService {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Qualifier("jdbcTddt4iots")
    private final JdbcTemplate jdbc;

    @Value("${tddt4iots.build.revision:local-development}")
    private String buildRevision;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registerDataset(Long trainingHistoryId, CreateFileTrainingReq request, Long personId) {
        List<ResearchDatasetSampleDTO> samples = request.getSamples() == null ? List.of() : request.getSamples();
        ObjectNode canonicalManifest = JSON.createObjectNode();
        canonicalManifest.put("datasetName", request.getDatasetName());
        canonicalManifest.put("datasetVersion", request.getDatasetVersion());
        canonicalManifest.put("schemaVersion", blankTo(request.getSchemaVersion(), "1.0"));
        canonicalManifest.set("samples", JSON.valueToTree(samples));
        String manifest = jsonNode(canonicalManifest);
        String manifestHash = sha256(manifest);
        String schemaVersion = blankTo(request.getSchemaVersion(), "1.0");
        long total = samples.size();
        long train = samples.stream().filter(item -> split(item).equals("TRAIN")).count();
        long validation = samples.stream().filter(item -> split(item).equals("VALIDATION")).count();
        long test = samples.stream().filter(item -> split(item).equals("TEST")).count();

        List<Map<String, Object>> existing = jdbc.queryForList("""
                SELECT id, manifest_sha256 FROM tddt4iots_research.ai_dataset_version
                WHERE name = ? AND version = ?
                """, request.getDatasetName(), request.getDatasetVersion());
        if (!existing.isEmpty() && !manifestHash.equals(existing.getFirst().get("manifest_sha256"))) {
            throw new IllegalArgumentException("La versión " + request.getDatasetName() + ":" + request.getDatasetVersion()
                    + " ya está congelada con un contenido diferente. Cree una nueva versión.");
        }

        Long datasetId = jdbc.queryForObject("""
                INSERT INTO tddt4iots_research.ai_dataset_version
                    (name, version, description, schema_version, manifest_sha256, source_commit, status,
                     total_items, train_items, validation_items, test_items, created_by_person_id, frozen_at)
                VALUES (?, ?, ?, ?, ?, ?, 'FROZEN', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (name, version) DO UPDATE SET
                    status = 'FROZEN', frozen_at = COALESCE(ai_dataset_version.frozen_at, CURRENT_TIMESTAMP)
                RETURNING id
                """, Long.class, request.getDatasetName(), request.getDatasetVersion(),
                "Dataset CASE revisado y versionado", schemaVersion, manifestHash, revision(),
                total, train, validation, test, personId);

        for (ResearchDatasetSampleDTO sample : samples) {
            String input = jsonNode(sample.getInputRequirements());
            String gold = jsonNode(sample.getGoldWorkspace());
            jdbc.update("""
                    INSERT INTO tddt4iots_research.ai_dataset_item
                        (dataset_version_id, external_project_id, project_hash, split, language,
                         source_type, input_requirements, gold_workspace, pii_removed, review_status, approved_at)
                    VALUES (?, ?, ?, ?, 'es', 'MIXED', ?::jsonb, ?::jsonb, TRUE, 'APPROVED', CURRENT_TIMESTAMP)
                    ON CONFLICT (dataset_version_id, project_hash) DO UPDATE SET
                        external_project_id = EXCLUDED.external_project_id,
                        split = EXCLUDED.split, input_requirements = EXCLUDED.input_requirements,
                        gold_workspace = EXCLUDED.gold_workspace, review_status = 'APPROVED',
                        approved_at = CURRENT_TIMESTAMP
                    """, datasetId, sample.getProjectId(), sha256(input + "\n" + gold), split(sample), input, gold);
        }

        jdbc.update("""
                INSERT INTO tddt4iots_research.ai_training_link(training_history_id, dataset_version_id)
                VALUES (?, ?) ON CONFLICT (training_history_id) DO UPDATE SET
                    dataset_version_id = EXCLUDED.dataset_version_id, updated_at = CURRENT_TIMESTAMP
                """, trainingHistoryId, datasetId);
        jdbc.update("""
                INSERT INTO tddt4iots_research.ai_artifact
                    (dataset_version_id, artifact_type, storage_path, sha256, mime_type)
                SELECT ?, 'DATASET_MANIFEST', ?, ?, 'application/json'
                WHERE NOT EXISTS (
                    SELECT 1 FROM tddt4iots_research.ai_artifact
                    WHERE dataset_version_id = ? AND artifact_type = 'DATASET_MANIFEST' AND sha256 = ?)
                """, datasetId, "db://research/ai_dataset_version/" + datasetId + "/manifest",
                manifestHash, datasetId, manifestHash);
        return datasetId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerTrainingStart(Long trainingHistoryId, JsonNode job) {
        ensureLegacyDatasetLink(trainingHistoryId);
        Map<String, Object> link = link(trainingHistoryId);
        Long datasetId = number(link.get("dataset_version_id"));
        String baseModel = job.path("model").asText("unknown");
        String status = job.path("status").asText("queued").toUpperCase(Locale.ROOT);
        String experimentStatus = experimentStatus(status);
        String code = "FT-HISTORY-" + trainingHistoryId;
        Long experimentId = jdbc.queryForObject("""
                INSERT INTO tddt4iots_research.ai_experiment
                    (code, name, research_question, hypothesis, method, dataset_version_id, base_model,
                     model_snapshot, prompt_version, schema_version, source_commit, parameters,
                     fixed_seed, status, created_by_person_id, started_at)
                SELECT ?, ?, ?, ?, 'FINE_TUNING', ?, ?, ?, 'case-workspace-v1', '1.0', ?, ?::jsonb,
                       ?, ?, h.id_person, CURRENT_TIMESTAMP
                FROM training_history h WHERE h.id = ?
                ON CONFLICT (code) DO UPDATE SET status = EXCLUDED.status,
                    model_snapshot = EXCLUDED.model_snapshot, parameters = EXCLUDED.parameters
                RETURNING id
                """, Long.class, code, "Fine-tuning CASE #" + trainingHistoryId,
                "¿Mejora el fine-tuning la generación estructural de modelos CASE?",
                "El modelo ajustado incrementará validez, completitud y trazabilidad del workspace generado.",
                datasetId, baseModel, baseModel, revision(), parameters(job), nullableLong(job, "seed"),
                experimentStatus, trainingHistoryId);

        Long runId = number(link.get("training_run_id"));
        if (runId == null) {
            runId = jdbc.queryForObject("""
                    INSERT INTO tddt4iots_research.ai_training_run
                        (experiment_id, provider_job_id, provider, base_model, resulting_model,
                         training_file_id, validation_file_id, status, epochs, batch_size,
                         learning_rate_multiplier, seed, trained_tokens, raw_provider_result, started_at)
                    VALUES (?, ?, 'OPENAI', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP)
                    ON CONFLICT (provider_job_id) WHERE provider_job_id IS NOT NULL DO UPDATE SET
                        status = EXCLUDED.status, raw_provider_result = EXCLUDED.raw_provider_result
                    RETURNING id
                    """, Long.class, experimentId, text(job, "id"), baseModel,
                    text(job, "fine_tuned_model"), text(job, "training_file"), text(job, "validation_file"),
                    status, nestedInt(job, "hyperparameters", "n_epochs"),
                    nestedInt(job, "hyperparameters", "batch_size"),
                    nestedDouble(job, "hyperparameters", "learning_rate_multiplier"),
                    nullableLong(job, "seed"), nullableLong(job, "trained_tokens"), jsonNode(job));
        }
        jdbc.update("""
                UPDATE tddt4iots_research.ai_training_link
                SET experiment_id = ?, training_run_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE training_history_id = ?
                """, experimentId, runId, trainingHistoryId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void synchronizeTraining(Long trainingHistoryId, JsonNode job, JsonNode checkpoints) {
        registerTrainingStart(trainingHistoryId, job);
        Map<String, Object> link = link(trainingHistoryId);
        Long runId = number(link.get("training_run_id"));
        Long experimentId = number(link.get("experiment_id"));
        String status = job.path("status").asText("unknown").toUpperCase(Locale.ROOT);
        jdbc.update("""
                UPDATE tddt4iots_research.ai_training_run SET
                    provider_job_id = COALESCE(?, provider_job_id), status = ?,
                    resulting_model = COALESCE(?, resulting_model), trained_tokens = COALESCE(?, trained_tokens),
                    raw_provider_result = ?::jsonb, error_message = ?,
                    finished_at = CASE WHEN ? IN ('SUCCEEDED','FAILED','CANCELLED') THEN CURRENT_TIMESTAMP ELSE finished_at END
                WHERE id = ?
                """, text(job, "id"), status, text(job, "fine_tuned_model"), nullableLong(job, "trained_tokens"),
                jsonNode(job), job.path("error").path("message").asText(null), status, runId);
        jdbc.update("""
                UPDATE tddt4iots_research.ai_experiment SET status = ?,
                    completed_at = CASE WHEN ? IN ('COMPLETED','FAILED','CANCELLED') THEN CURRENT_TIMESTAMP ELSE completed_at END
                WHERE id = ?
                """, experimentStatus(status), experimentStatus(status), experimentId);

        if (checkpoints != null && checkpoints.isArray()) {
            for (JsonNode checkpoint : checkpoints) {
                JsonNode metrics = checkpoint.path("metrics");
                jdbc.update("""
                        INSERT INTO tddt4iots_research.ai_training_checkpoint
                            (training_run_id, provider_checkpoint_id, step_number, training_loss,
                             validation_loss, training_token_accuracy, validation_token_accuracy, recorded_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (training_run_id, step_number) DO UPDATE SET
                            provider_checkpoint_id = EXCLUDED.provider_checkpoint_id,
                            training_loss = EXCLUDED.training_loss, validation_loss = EXCLUDED.validation_loss,
                            training_token_accuracy = EXCLUDED.training_token_accuracy,
                            validation_token_accuracy = EXCLUDED.validation_token_accuracy
                        """, runId, text(checkpoint, "id"), checkpoint.path("step_number").asInt(0),
                        firstDouble(metrics, "train_loss", "training_loss"),
                        firstDouble(metrics, "full_valid_loss", "valid_loss", "validation_loss"),
                        firstDouble(metrics, "train_mean_token_accuracy", "training_mean_token_accuracy"),
                        firstDouble(metrics, "full_valid_mean_token_accuracy", "valid_mean_token_accuracy", "validation_token_accuracy"),
                        timestamp(checkpoint.path("created_at").asLong(Instant.now().getEpochSecond())));
            }
            updateFinalTrainingMetrics(runId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long recordInference(Long trainingHistoryId, JsonNode inputRequirements, String rawOutput,
                                JsonNode parsedWorkspace, ArrayNode validationErrors, long latencyMs,
                                String modelSnapshot, JsonNode usage, String errorMessage) {
        ensureExperiment(trainingHistoryId, modelSnapshot);
        Long experimentId = number(link(trainingHistoryId).get("experiment_id"));
        boolean success = parsedWorkspace != null && errorMessage == null;
        Long inferenceId = jdbc.queryForObject("""
                INSERT INTO tddt4iots_research.ai_inference_run
                    (experiment_id, dataset_item_id, training_history_id, repetition, model_snapshot,
                     prompt_version, schema_version, input_requirements, raw_output, parsed_workspace,
                     validation_errors, validation_warnings, success, refused, repair_count, retry_count,
                     prompt_tokens, completion_tokens, latency_ms, error_message)
                VALUES (?, NULL, ?, 1, ?, 'case-workspace-v1', '1.0', ?::jsonb, ?, ?::jsonb,
                        ?::jsonb, '[]'::jsonb, ?, FALSE, 0, 0, ?, ?, ?, ?)
                RETURNING id
                """, Long.class, experimentId, trainingHistoryId, blankTo(modelSnapshot, "unknown"),
                jsonNode(inputRequirements), rawOutput, jsonNode(parsedWorkspace), jsonNode(validationErrors), success,
                usage == null ? null : nullableInt(usage, "prompt_tokens"),
                usage == null ? null : nullableInt(usage, "completion_tokens"), latencyMs, errorMessage);
        saveInferenceMetrics(inferenceId, parsedWorkspace, success);
        saveInferenceArtifacts(experimentId, inferenceId, rawOutput, parsedWorkspace);
        return inferenceId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long recordHumanReview(Long datasetItemId, Long reviewerPersonId, JsonNode review) {
        String decision = review.path("decision").asText("REVISE").toUpperCase(Locale.ROOT);
        if (!java.util.Set.of("APPROVE", "REVISE", "REJECT").contains(decision)) {
            throw new IllegalArgumentException("La decisión debe ser APPROVE, REVISE o REJECT.");
        }
        Long reviewId = jdbc.queryForObject("""
                INSERT INTO tddt4iots_research.ai_human_review
                    (dataset_item_id, reviewer_person_id, review_round, correctness, completeness,
                     consistency, readability, usability, correction_seconds, added_elements,
                     changed_elements, deleted_elements, decision, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (dataset_item_id, reviewer_person_id, review_round) DO UPDATE SET
                    correctness = EXCLUDED.correctness, completeness = EXCLUDED.completeness,
                    consistency = EXCLUDED.consistency, readability = EXCLUDED.readability,
                    usability = EXCLUDED.usability, correction_seconds = EXCLUDED.correction_seconds,
                    added_elements = EXCLUDED.added_elements, changed_elements = EXCLUDED.changed_elements,
                    deleted_elements = EXCLUDED.deleted_elements, decision = EXCLUDED.decision,
                    notes = EXCLUDED.notes, reviewed_at = CURRENT_TIMESTAMP
                RETURNING id
                """, Long.class, datasetItemId, reviewerPersonId, review.path("reviewRound").asInt(1),
                score(review, "correctness"), score(review, "completeness"), score(review, "consistency"),
                score(review, "readability"), score(review, "usability"), nullableInt(review, "correctionSeconds"),
                review.path("addedElements").asInt(0), review.path("changedElements").asInt(0),
                review.path("deletedElements").asInt(0), decision,
                review.path("notes").asText(null));
        String itemStatus = switch (decision) {
            case "APPROVE" -> "APPROVED";
            case "REJECT" -> "REJECTED";
            default -> "IN_REVIEW";
        };
        jdbc.update("UPDATE tddt4iots_research.ai_dataset_item SET review_status = ? WHERE id = ?",
                itemStatus, datasetItemId);
        return reviewId;
    }

    public ObjectNode summary() {
        ObjectNode result = JSON.createObjectNode();
        result.set("counts", JSON.valueToTree(jdbc.queryForMap("""
                SELECT (SELECT COUNT(*) FROM tddt4iots_research.ai_dataset_version) datasets,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_dataset_item) dataset_items,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_experiment) experiments,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_training_run) training_runs,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_training_checkpoint) checkpoints,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_inference_run) inferences,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_metric_result) metrics,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_human_review) human_reviews,
                       (SELECT COUNT(*) FROM tddt4iots_research.ai_artifact) artifacts
                """)));
        result.set("training", JSON.valueToTree(jdbc.queryForList("""
                SELECT * FROM tddt4iots_research.v_training_research_summary
                ORDER BY training_history_id DESC LIMIT 100
                """)));
        result.set("metrics", JSON.valueToTree(jdbc.queryForList("""
                SELECT * FROM tddt4iots_research.v_metric_summary
                ORDER BY experiment_id DESC, metric_group, metric_name
                """)));
        return result;
    }

    public List<Map<String, Object>> reviewItems() {
        return jdbc.queryForList("""
                SELECT i.id, d.name AS dataset_name, d.version AS dataset_version,
                       i.external_project_id, i.split, i.domain, i.complexity,
                       i.review_status, i.input_requirements, i.gold_workspace,
                       COUNT(r.id) AS review_count
                FROM tddt4iots_research.ai_dataset_item i
                JOIN tddt4iots_research.ai_dataset_version d ON d.id = i.dataset_version_id
                LEFT JOIN tddt4iots_research.ai_human_review r ON r.dataset_item_id = i.id
                GROUP BY i.id, d.name, d.version
                ORDER BY CASE i.review_status WHEN 'PENDING' THEN 0 WHEN 'IN_REVIEW' THEN 1 ELSE 2 END,
                         i.id DESC LIMIT 200
                """);
    }

    public String exportTrainingCsv() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT training_history_id, dataset_version_id, experiment_id, training_run_id,
                       provider_job_id, provider_status, progress_percent, last_monitored_at,
                       base_model, resulting_model, trained_tokens, training_loss, validation_loss,
                       validation_token_accuracy, checkpoint_count, inference_count
                FROM tddt4iots_research.v_training_research_summary
                ORDER BY training_history_id
                """);
        String[] columns = {"training_history_id", "dataset_version_id", "experiment_id", "training_run_id",
                "provider_job_id", "provider_status", "progress_percent", "last_monitored_at", "base_model",
                "resulting_model", "trained_tokens", "training_loss", "validation_loss",
                "validation_token_accuracy", "checkpoint_count", "inference_count"};
        StringBuilder csv = new StringBuilder(String.join(",", columns)).append('\n');
        for (Map<String, Object> row : rows) {
            for (int index = 0; index < columns.length; index++) {
                if (index > 0) csv.append(',');
                csv.append(csvCell(row.get(columns[index])));
            }
            csv.append('\n');
        }
        return csv.toString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int backfillExistingTrainings() {
        List<Map<String, Object>> histories = jdbc.queryForList("""
                SELECT h.id, h.id_person, h.path_file_json, h.count_row_data_trining,
                       h.result_trining, h.provider_job_id, h.provider_status
                FROM training_history h
                WHERE h.result_trining IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM tddt4iots_research.ai_training_link l WHERE l.training_history_id = h.id)
                ORDER BY h.id
                """);
        int imported = 0;
        for (Map<String, Object> history : histories) {
            try {
                Long historyId = number(history.get("id"));
                Long datasetId = legacyDataset(history);
                jdbc.update("""
                        INSERT INTO tddt4iots_research.ai_training_link(training_history_id, dataset_version_id)
                        VALUES (?, ?) ON CONFLICT (training_history_id) DO NOTHING
                        """, historyId, datasetId);
                JsonNode envelope = parse(String.valueOf(history.get("result_trining")));
                JsonNode job = envelope.path("data");
                if (job.isObject() && !job.isEmpty()) registerTrainingStart(historyId, job);
                imported++;
            } catch (Exception error) {
                log.warn("Could not backfill scientific training {}: {}", history.get("id"), error.getMessage());
            }
        }
        synchronizeHistoricalCheckpoints();
        return imported;
    }

    private void synchronizeHistoricalCheckpoints() {
        List<Map<String, Object>> histories = jdbc.queryForList("""
                SELECT h.id, h.result_trining
                FROM training_history h
                JOIN tddt4iots_research.ai_training_link l ON l.training_history_id = h.id
                WHERE h.result_trining IS NOT NULL
                """);
        for (Map<String, Object> history : histories) {
            try {
                Long historyId = number(history.get("id"));
                JsonNode job = parse(String.valueOf(history.get("result_trining"))).path("data");
                if (!job.isObject() || job.isEmpty()) continue;
                ArrayNode checkpoints = JSON.createArrayNode();
                for (Map<String, Object> row : jdbc.queryForList("""
                        SELECT provider_checkpoint_id, fine_tuned_model, step_number,
                               provider_created_at, metrics_payload
                        FROM training_monitor_checkpoint WHERE training_history_id = ?
                        ORDER BY step_number
                        """, historyId)) {
                    ObjectNode checkpoint = checkpoints.addObject();
                    checkpoint.put("id", String.valueOf(row.get("provider_checkpoint_id")));
                    checkpoint.put("fine_tuned_model_checkpoint", String.valueOf(row.get("fine_tuned_model")));
                    if (row.get("step_number") != null) checkpoint.put("step_number", ((Number) row.get("step_number")).longValue());
                    if (row.get("provider_created_at") != null) checkpoint.put("created_at", ((Number) row.get("provider_created_at")).longValue());
                    checkpoint.set("metrics", parse(String.valueOf(row.get("metrics_payload"))));
                }
                synchronizeTraining(historyId, job, checkpoints);
            } catch (Exception error) {
                log.warn("Could not synchronize historical scientific checkpoints for {}: {}", history.get("id"), error.getMessage());
            }
        }
    }

    private Long legacyDataset(Map<String, Object> history) {
        Long historyId = number(history.get("id"));
        String hash = sha256(historyId + "|" + history.get("path_file_json") + "|" + history.get("count_row_data_trining"));
        int count = history.get("count_row_data_trining") == null ? 0 : ((Number) history.get("count_row_data_trining")).intValue();
        return jdbc.queryForObject("""
                INSERT INTO tddt4iots_research.ai_dataset_version
                    (name, version, description, schema_version, manifest_sha256, source_commit,
                     status, total_items, train_items, created_by_person_id, frozen_at)
                VALUES (?, 'legacy-1.0', ?, 'legacy', ?, 'legacy-import', 'ARCHIVED', ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (name, version) DO UPDATE SET total_items = EXCLUDED.total_items
                RETURNING id
                """, Long.class, "training-history-" + historyId,
                "Dataset histórico importado; las muestras originales no están disponibles de forma estructurada.",
                hash, count, count, history.get("id_person"));
    }

    private void ensureLegacyDatasetLink(Long trainingHistoryId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM tddt4iots_research.ai_training_link WHERE training_history_id = ?", Integer.class, trainingHistoryId);
        if (count != null && count > 0) return;
        Map<String, Object> history = jdbc.queryForMap("""
                SELECT id, id_person, path_file_json, count_row_data_trining
                FROM training_history WHERE id = ?
                """, trainingHistoryId);
        Long datasetId = legacyDataset(history);
        jdbc.update("INSERT INTO tddt4iots_research.ai_training_link(training_history_id, dataset_version_id) VALUES (?, ?)", trainingHistoryId, datasetId);
    }

    private void ensureExperiment(Long historyId, String model) {
        ensureLegacyDatasetLink(historyId);
        Map<String, Object> current = link(historyId);
        if (current.get("experiment_id") != null) return;
        ObjectNode job = JSON.createObjectNode();
        job.put("model", blankTo(model, "unknown"));
        job.put("status", "succeeded");
        registerTrainingStart(historyId, job);
    }

    private Map<String, Object> link(Long historyId) {
        return jdbc.queryForMap("SELECT * FROM tddt4iots_research.ai_training_link WHERE training_history_id = ?", historyId);
    }

    private void updateFinalTrainingMetrics(Long runId) {
        jdbc.update("""
                UPDATE tddt4iots_research.ai_training_run r SET
                    training_loss = c.training_loss,
                    validation_loss = c.validation_loss,
                    validation_token_accuracy = c.validation_token_accuracy
                FROM tddt4iots_research.ai_training_checkpoint c
                WHERE r.id = ? AND c.training_run_id = r.id
                  AND c.step_number = (SELECT MAX(step_number) FROM tddt4iots_research.ai_training_checkpoint WHERE training_run_id = r.id)
                """, runId);
    }

    private void saveInferenceMetrics(Long inferenceId, JsonNode workspace, boolean success) {
        metric(inferenceId, "QUALITY", "schema_valid", success ? 1D : 0D, "case-metrics-v1");
        if (!success || workspace == null) return;
        JsonNode diagrams = workspace.path("diagrams");
        int diagramCount = diagrams.isArray() ? diagrams.size() : 0;
        int actors = 0, useCases = 0, sequences = 0;
        if (diagrams.isArray()) for (JsonNode diagram : diagrams) {
            actors += diagram.path("actors").isArray() ? diagram.path("actors").size() : 0;
            JsonNode cases = diagram.path("useCases");
            useCases += cases.isArray() ? cases.size() : 0;
            if (cases.isArray()) for (JsonNode useCase : cases) {
                if (useCase.path("sequence").path("messages").isArray()
                        && !useCase.path("sequence").path("messages").isEmpty()) sequences++;
            }
        }
        metric(inferenceId, "STRUCTURE", "diagram_count", diagramCount, "case-metrics-v1");
        metric(inferenceId, "STRUCTURE", "actor_count", actors, "case-metrics-v1");
        metric(inferenceId, "STRUCTURE", "use_case_count", useCases, "case-metrics-v1");
        metric(inferenceId, "COMPLETENESS", "sequence_coverage", useCases == 0 ? 0D : (double) sequences / useCases, "case-metrics-v1");
    }

    private void metric(Long inferenceId, String group, String name, double value, String evaluator) {
        jdbc.update("""
                INSERT INTO tddt4iots_research.ai_metric_result
                    (inference_run_id, metric_group, metric_name, metric_value, evaluator_version)
                VALUES (?, ?, ?, ?, ?) ON CONFLICT (inference_run_id, metric_name, evaluator_version)
                DO UPDATE SET metric_value = EXCLUDED.metric_value
                """, inferenceId, group, name, value, evaluator);
    }

    private void saveInferenceArtifacts(Long experimentId, Long inferenceId, String raw, JsonNode workspace) {
        if (raw != null) artifact(experimentId, inferenceId, "RAW_RESPONSE", "db://research/ai_inference_run/" + inferenceId + "/raw_output", raw);
        if (workspace != null) artifact(experimentId, inferenceId, "WORKSPACE", "db://research/ai_inference_run/" + inferenceId + "/parsed_workspace", jsonNode(workspace));
    }

    private void artifact(Long experimentId, Long inferenceId, String type, String path, String content) {
        jdbc.update("""
                INSERT INTO tddt4iots_research.ai_artifact
                    (experiment_id, inference_run_id, artifact_type, storage_path, sha256, mime_type)
                VALUES (?, ?, ?, ?, ?, 'application/json')
                """, experimentId, inferenceId, type, path, sha256(content));
    }

    private String parameters(JsonNode job) {
        ObjectNode parameters = JSON.createObjectNode();
        parameters.set("hyperparameters", job.path("hyperparameters"));
        parameters.put("provider", "OPENAI");
        parameters.put("buildRevision", revision());
        return jsonNode(parameters);
    }

    private String revision() { return blankTo(buildRevision, "local-development"); }
    private String split(ResearchDatasetSampleDTO item) { return blankTo(item.getSplit(), "TRAIN").toUpperCase(Locale.ROOT); }
    private String json(Object value) { try { return JSON.writeValueAsString(value); } catch (Exception e) { throw new IllegalArgumentException(e); } }
    private JsonNode parse(String value) { try { return JSON.readTree(value); } catch (Exception e) { return JSON.createObjectNode(); } }
    private String jsonNode(JsonNode value) { return value == null ? "null" : value.toString(); }
    private String sha256(String value) { try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
    private Long number(Object value) { return value == null ? null : ((Number) value).longValue(); }
    private String text(JsonNode node, String field) { return node.path(field).isNull() || node.path(field).isMissingNode() ? null : node.path(field).asText(null); }
    private Long nullableLong(JsonNode node, String field) { return node.path(field).isNumber() ? node.path(field).asLong() : null; }
    private Integer nullableInt(JsonNode node, String field) { return node.path(field).isNumber() ? node.path(field).asInt() : null; }
    private Integer nestedInt(JsonNode node, String parent, String field) { return node.path(parent).path(field).isNumber() ? node.path(parent).path(field).asInt() : null; }
    private Double nestedDouble(JsonNode node, String parent, String field) { return node.path(parent).path(field).isNumber() ? node.path(parent).path(field).asDouble() : null; }
    private Double firstDouble(JsonNode node, String... fields) { for (String field : fields) if (node.path(field).isNumber()) return node.path(field).asDouble(); return null; }
    private Integer score(JsonNode review, String field) { int value = review.path(field).asInt(0); return value >= 1 && value <= 5 ? value : null; }
    private String blankTo(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private Timestamp timestamp(long epochSeconds) { return Timestamp.from(Instant.ofEpochSecond(epochSeconds)); }
    private String csvCell(Object value) { String text = value == null ? "" : String.valueOf(value); return "\"" + text.replace("\"", "\"\"") + "\""; }
    private String experimentStatus(String status) { return switch (status.toUpperCase(Locale.ROOT)) { case "SUCCEEDED", "COMPLETED" -> "COMPLETED"; case "FAILED" -> "FAILED"; case "CANCELLED" -> "CANCELLED"; case "QUEUED", "VALIDATING_FILES" -> "PLANNED"; default -> "RUNNING"; }; }
}
