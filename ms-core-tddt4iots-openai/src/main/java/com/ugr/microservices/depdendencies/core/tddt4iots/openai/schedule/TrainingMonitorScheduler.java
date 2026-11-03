package com.ugr.microservices.depdendencies.core.tddt4iots.openai.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc.GrpcTrainingModelOpenAi;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ResearchPersistenceService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.*;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrainingMonitorScheduler {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern GRPC_JSON = Pattern.compile("Processing result:\\s*(\\{.*})", Pattern.DOTALL);
    private final TrainingHistoryRepository historyRepository;
    private final TrainingMonitorSnapshotRepository snapshotRepository;
    private final TrainingMonitorEventRepository eventRepository;
    private final TrainingMonitorCheckpointRepository checkpointRepository;
    private final GrpcTrainingModelOpenAi grpcClient;
    private final ResearchPersistenceService researchPersistenceService;

    @Scheduled(initialDelayString = "${tddt4iots.training-monitor.initial-delay-ms:30000}",
            fixedDelayString = "${tddt4iots.training-monitor.delay-ms:60000}")
    @Transactional
    public void monitorActiveTrainings() {
        for (TrainingHistory history : historyRepository.findMonitorableTrainings()) {
            monitor(history);
        }
    }

    private void monitor(TrainingHistory history) {
        long started = System.currentTimeMillis();
        try {
            String jobId = resolveJobId(history);
            if (jobId == null) {
                history.setProviderStatus("legacy_unmonitorable");
                historyRepository.save(history);
                return;
            }
            ObjectNode request = JSON.createObjectNode();
            request.put("operation", "training_status");
            request.put("openAiSecretKey", history.getIdPerson().getOpenaiSecretKey());
            request.put("jobId", jobId);
            String grpcResponse = grpcClient.useModel(JSON.writeValueAsString(request));
            Matcher matcher = GRPC_JSON.matcher(grpcResponse);
            if (!matcher.find()) throw new IllegalStateException("The monitor service returned no JSON envelope");
            JsonNode envelope = JSON.readTree(matcher.group(1));
            if (!"OK".equals(envelope.path("status").asText())) {
                throw new IllegalStateException(envelope.path("message").asText("Provider monitor error"));
            }
            JsonNode data = envelope.path("data");
            JsonNode job = data.path("job");
            String providerStatus = job.path("status").asText("unknown");
            Double progress = data.path("progress_percent").isNumber() ? data.path("progress_percent").asDouble() : null;

            history.setProviderJobId(jobId);
            history.setProviderStatus(providerStatus);
            history.setProgressPercent(progress);
            history.setLastMonitoredAt(new Date());
            history.setMonitoringFailures(0);
            history.setStatus(statusCode(providerStatus));
            ObjectNode compatibleResult = JSON.createObjectNode();
            compatibleResult.put("code", 0).put("status", "OK").put("message", "Training status monitored");
            compatibleResult.set("data", job);
            history.setResultTrining(JSON.writeValueAsString(compatibleResult));
            historyRepository.save(history);

            saveSnapshot(history, providerStatus, progress, matcher.group(1), null, started);
            saveEvents(history, data.path("events"));
            saveCheckpoints(history, data.path("checkpoints"));
            try {
                researchPersistenceService.synchronizeTraining(history.getId(), job, data.path("checkpoints"));
            } catch (Exception researchError) {
                log.warn("Operational monitoring succeeded, but scientific synchronization failed for {}: {}",
                        jobId, researchError.getMessage());
            }
            log.info("Training {} monitored: {}", jobId, providerStatus);
        } catch (Exception error) {
            int failures = (history.getMonitoringFailures() == null ? 0 : history.getMonitoringFailures()) + 1;
            history.setMonitoringFailures(failures);
            history.setLastMonitoredAt(new Date());
            if (failures >= 5) history.setProviderStatus("monitor_error");
            historyRepository.save(history);
            saveSnapshot(history, history.getProviderStatus() == null ? "monitor_error" : history.getProviderStatus(),
                    history.getProgressPercent(), null, error.getMessage(), started);
            log.warn("Could not monitor training {}: {}", history.getId(), error.getMessage());
        }
    }

    private String resolveJobId(TrainingHistory history) throws Exception {
        if (history.getProviderJobId() != null && !history.getProviderJobId().isBlank()) return history.getProviderJobId();
        JsonNode root = JSON.readTree(history.getResultTrining());
        return root.path("data").path("id").asText(null);
    }

    private void saveSnapshot(TrainingHistory history, String status, Double progress, String payload, String error, long started) {
        TrainingMonitorSnapshot snapshot = new TrainingMonitorSnapshot();
        snapshot.setTrainingHistory(history);
        snapshot.setProviderJobId(history.getProviderJobId() == null ? "unknown" : history.getProviderJobId());
        snapshot.setProviderStatus(status);
        snapshot.setProgressPercent(progress);
        snapshot.setCheckedAt(new Date());
        snapshot.setDurationMs(System.currentTimeMillis() - started);
        snapshot.setResponsePayload(payload);
        snapshot.setErrorMessage(error);
        snapshotRepository.save(snapshot);
    }

    private void saveEvents(TrainingHistory history, JsonNode events) {
        if (!events.isArray()) return;
        for (JsonNode node : events) {
            String id = node.path("id").asText(null);
            if (id == null || eventRepository.existsByProviderEventId(id)) continue;
            TrainingMonitorEvent event = new TrainingMonitorEvent();
            event.setTrainingHistory(history);
            event.setProviderEventId(id);
            event.setLevel(node.path("level").asText(null));
            event.setMessage(node.path("message").asText(null));
            event.setProviderCreatedAt(node.path("created_at").isNumber() ? node.path("created_at").asLong() : null);
            event.setDataPayload(node.path("data").toString());
            event.setCapturedAt(new Date());
            eventRepository.save(event);
        }
    }

    private void saveCheckpoints(TrainingHistory history, JsonNode checkpoints) {
        if (!checkpoints.isArray()) return;
        for (JsonNode node : checkpoints) {
            String id = node.path("id").asText(null);
            if (id == null || checkpointRepository.existsByProviderCheckpointId(id)) continue;
            TrainingMonitorCheckpoint checkpoint = new TrainingMonitorCheckpoint();
            checkpoint.setTrainingHistory(history);
            checkpoint.setProviderCheckpointId(id);
            checkpoint.setFineTunedModel(node.path("fine_tuned_model_checkpoint").asText(null));
            checkpoint.setStepNumber(node.path("step_number").isNumber() ? node.path("step_number").asLong() : null);
            checkpoint.setProviderCreatedAt(node.path("created_at").isNumber() ? node.path("created_at").asLong() : null);
            checkpoint.setMetricsPayload(node.path("metrics").toString());
            checkpoint.setCapturedAt(new Date());
            checkpointRepository.save(checkpoint);
        }
    }

    private char statusCode(String status) {
        if ("succeeded".equals(status)) return 'S';
        if ("failed".equals(status) || "cancelled".equals(status)) return 'E';
        if ("running".equals(status)) return 'R';
        return 'Q';
    }
}
