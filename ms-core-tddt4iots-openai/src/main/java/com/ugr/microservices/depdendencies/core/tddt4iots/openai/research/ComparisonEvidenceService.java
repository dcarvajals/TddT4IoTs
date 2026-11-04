package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ComparisonEvidenceService {
    private static final ObjectMapper JSON = new ObjectMapper();
    @Qualifier("jdbcTddt4iots")
    private final JdbcTemplate jdbc;
    @Value("${tddt4iots.build.revision:local-development}")
    private String revision;

    public static void validate(GenerateCaseWorkspaceReqDTO input) {
        if (input.getMaxOutputTokens() != null && (input.getMaxOutputTokens() < 1 || input.getMaxOutputTokens() > 128000))
            throw new IllegalArgumentException("El límite de salida debe estar entre 1 y 128000 tokens.");
        if (input.getTemperature() != null && (!Double.isFinite(input.getTemperature()) || input.getTemperature() < 0 || input.getTemperature() > 2))
            throw new IllegalArgumentException("La temperatura debe estar entre 0 y 2.");
        if (input.getReasoningEffort() != null && !input.getReasoningEffort().isBlank()
                && !Set.of("none", "minimal", "low", "medium", "high", "xhigh", "max").contains(input.getReasoningEffort()))
            throw new IllegalArgumentException("Nivel de razonamiento no válido.");
        ComparisonRunContextDTO context = input.getResearch();
        if (context == null) return;
        if (input.getProjectId() == null || input.getProjectId().isBlank() || input.getProjectId().length() > 512)
            throw new IllegalArgumentException("El registro de investigación requiere un proyecto.");
        requireCode(context.getExperimentCode(), 100, "experimento");
        requireCode(context.getSystemCode(), 100, "sistema");
        requireCode(context.getRequirementsVersion(), 40, "versión de requisitos");
        if (!Set.of("PILOT", "EVALUATION").contains(Objects.toString(context.getPhase(), "")))
            throw new IllegalArgumentException("La fase debe ser PILOT o EVALUATION.");
        if (context.getRepetition() == null || context.getRepetition() < 1
                || context.getAttempt() == null || context.getAttempt() < 1)
            throw new IllegalArgumentException("Repetición e intento deben ser enteros positivos.");
    }

    private static void requireCode(String value, int max, String label) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0," + (max - 1) + "}"))
            throw new IllegalArgumentException("Identificador de " + label + " no válido: use letras, números, punto, guion o guion bajo.");
    }

    public static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    /** Whitelist evidence fields: API credentials must never enter the research dataset. */
    static ObjectNode requestEvidence(GrpcUseModelReqDTO request) {
        ObjectNode result = JSON.createObjectNode();
        result.put("model", request.getModel());
        result.set("messages", JSON.valueToTree(request.getMessages()));
        result.put("responseSchema", request.getResponseSchema());
        result.put("maxOutputTokens", request.getMaxOutputTokens());
        result.put("messagesSha256", sha256(result.path("messages").toString()));
        result.put("adapter", "python-chat-completions-v3");
        result.put("temperature", request.getTemperature());
        result.put("reasoningEffort", request.getReasoningEffort());
        result.put("sdkMaxRetries", 0);
        result.put("timeoutSeconds", 600);
        return result;
    }

    @Transactional
    public UUID begin(Long researcherId, Long ownerId, Long registrationId,
                      GenerateCaseWorkspaceReqDTO input, String effectiveText, GrpcUseModelReqDTO request) {
        return begin(researcherId, ownerId, registrationId, "TRAINED_MODEL", input, effectiveText, request);
    }

    @Transactional
    public UUID begin(Long researcherId, Long ownerId, Long registrationId, String registrationKind,
                      GenerateCaseWorkspaceReqDTO input, String effectiveText, GrpcUseModelReqDTO request) {
        validate(input);
        if (input.getResearch() == null) return null;
        if (!Set.of("BASE_MODEL", "TRAINED_MODEL").contains(registrationKind)) throw new IllegalArgumentException("Tipo de registro no válido.");
        ComparisonRunContextDTO c = input.getResearch();
        String hash = sha256(effectiveText);
        jdbc.update("""
                INSERT INTO tddt4iots_research.ai_comparison_case
                    (researcher_id, experiment_code, phase, system_code, requirements_version, input_sha256, effective_input)
                VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING
                """, researcherId, c.getExperimentCode(), c.getPhase(), c.getSystemCode(), c.getRequirementsVersion(), hash, effectiveText);
        Map<String, Object> item = jdbc.queryForMap("""
                SELECT id, input_sha256 FROM tddt4iots_research.ai_comparison_case
                WHERE researcher_id = ? AND experiment_code = ? AND phase = ? AND system_code = ? AND requirements_version = ?
                """, researcherId, c.getExperimentCode(), c.getPhase(), c.getSystemCode(), c.getRequirementsVersion());
        if (!hash.equals(item.get("input_sha256")))
            throw new IllegalArgumentException("Los requisitos difieren de la versión fijada para este sistema. Use la misma entrada o cree una nueva versión de requisitos.");
        UUID id = UUID.randomUUID();
        try {
            jdbc.update("""
                    INSERT INTO tddt4iots_research.ai_comparison_run
                        (id, case_id, researcher_id, project_id, model_registration_id, model_owner_id, model_registration_kind,
                         requested_model, repetition, attempt, prompt_version, schema_version, source_commit,
                         request_evidence, source_documents)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'case-workspace-v2', '1.0', ?, ?::jsonb, ?::jsonb)
                    """, id, item.get("id"), researcherId, input.getProjectId(), registrationId, ownerId,
                    registrationKind, request.getModel(), c.getRepetition(), c.getAttempt(),
                    revision == null || revision.isBlank() ? "local-development" : revision,
                    requestEvidence(request).toString(), JSON.valueToTree(input.getDocuments()).toString());
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Esta repetición e intento ya están registrados para el modelo. Consulte la evidencia existente o indique un nuevo intento.");
        }
        return id;
    }

    public void finish(UUID id, String status, String providerResponse, String rawOutput,
                       JsonNode workspace, String returnedModel, JsonNode usage, long latency, String error) {
        if (id == null) return;
        int updated = jdbc.update("""
                UPDATE tddt4iots_research.ai_comparison_run SET status = ?, provider_response = ?, raw_output = ?,
                    parsed_workspace = ?::jsonb, returned_model = ?, usage = ?::jsonb, latency_ms = ?,
                    error_message = ?, automatic_metrics = ?::jsonb, finished_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'STARTED'
                """, status, providerResponse, rawOutput, optionalJson(workspace),
                returnedModel, optionalJson(usage), latency, error,
                workspace == null ? null : ComparisonMetrics.analyze(workspace, null).toString(), id);
        if (updated != 1) throw new IllegalStateException("No se pudo finalizar la evidencia de generación " + id);
    }

    private static String optionalJson(JsonNode value) {
        // JsonNode.path() returns MissingNode for absent provider fields. Its empty
        // string representation is not valid JSON and must never reach a jsonb cast.
        return value == null || value.isMissingNode() || value.isNull() ? null : value.toString();
    }

    public void reportImport(Long researcherId, UUID id, JsonNode report) {
        String status = report.path("status").asText();
        if (!Set.of("IMPORTED", "VALIDATION_FAILED", "IMPORT_FAILED", "CAPTURE_FAILED", "DECLINED").contains(status))
            throw new IllegalArgumentException("Estado de importación no válido.");
        JsonNode snapshot = report.get("workspace");
        if (status.equals("IMPORTED") && (snapshot == null || !snapshot.isObject()
                || !snapshot.path("useCaseDiagrams").isArray() || !snapshot.path("classModel").isObject()))
            throw new IllegalArgumentException("Se requiere el UML importado antes de las correcciones.");
        ObjectNode details = JSON.createObjectNode();
        details.set("errors", report.path("errors"));
        details.set("warnings", report.path("warnings"));
        Map<String,Object> generation = detail(researcherId, id);
        JsonNode raw = (JsonNode) generation.get("parsed_workspace");
        ObjectNode metrics = ComparisonMetrics.analyze(raw, status.equals("IMPORTED") ? snapshot : null);
        int updated = jdbc.update("""
                UPDATE tddt4iots_research.ai_comparison_run SET client_status = ?, client_report = ?::jsonb,
                    original_imported_workspace = ?::jsonb, automatic_metrics = ?::jsonb, client_reported_at = CURRENT_TIMESTAMP
                WHERE id = ? AND researcher_id = ? AND status = 'SERVER_VALIDATED' AND client_status IS NULL
                """, status, details.toString(), status.equals("IMPORTED") ? snapshot.toString() : null, metrics.toString(), id, researcherId);
        if (updated != 1)
            throw new IllegalArgumentException("La ejecución no está disponible o su resultado de importación ya fue registrado.");
    }

    public List<Map<String, Object>> list(Long researcherId, String projectId) {
        return jdbc.queryForList("""
                SELECT r.id, r.project_id, c.experiment_code, c.phase, c.system_code, c.requirements_version,
                    c.input_sha256, r.model_registration_kind, r.requested_model, r.returned_model, r.repetition, r.attempt, r.status,
                    r.client_status, r.latency_ms, r.usage->>'prompt_tokens' AS prompt_tokens,
                    r.usage->>'completion_tokens' AS completion_tokens, r.usage->>'total_tokens' AS total_tokens,
                    r.error_message, r.started_at
                FROM tddt4iots_research.ai_comparison_run r JOIN tddt4iots_research.ai_comparison_case c ON c.id = r.case_id
                WHERE r.researcher_id = ? AND (?::text IS NULL OR r.project_id = ?) ORDER BY r.started_at DESC
                """, researcherId, projectId, projectId);
    }

    public Map<String, Object> detail(Long researcherId, UUID id) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT r.*, c.experiment_code, c.phase, c.system_code, c.requirements_version,
                    c.input_sha256, c.effective_input
                FROM tddt4iots_research.ai_comparison_run r JOIN tddt4iots_research.ai_comparison_case c ON c.id = r.case_id
                WHERE r.id = ? AND r.researcher_id = ?
                """, id, researcherId);
        if (rows.isEmpty()) throw new IllegalArgumentException("Ejecución no disponible.");
        Map<String, Object> result = new LinkedHashMap<>(rows.getFirst());
        for (String field : List.of("request_evidence", "source_documents", "parsed_workspace", "usage", "client_report", "original_imported_workspace", "automatic_metrics")) {
            Object value = result.get(field);
            if (value != null) {
                try { result.put(field, JSON.readTree(value.toString())); }
                catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("Evidencia JSON dañada.", e); }
            }
        }
        return result;
    }

    public String csv(Long researcherId, String projectId) {
        String[] fields = {"id", "project_id", "experiment_code", "phase", "system_code", "requirements_version",
                "input_sha256", "model_registration_kind", "requested_model", "returned_model", "repetition", "attempt", "status", "client_status", "latency_ms", "prompt_tokens", "completion_tokens", "total_tokens", "error_message", "started_at"};
        StringBuilder result = new StringBuilder(String.join(",", fields)).append('\n');
        for (Map<String, Object> row : list(researcherId, projectId)) {
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) result.append(',');
                String value = Objects.toString(row.get(fields[i]), "");
                if (value.matches("^[=+@\\-].*")) value = "'" + value;
                result.append('"').append(value.replace("\"", "\"\"")).append('"');
            }
            result.append('\n');
        }
        return result.toString();
    }
}
