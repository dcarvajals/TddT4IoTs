package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelUseToolBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.TrainingHistoryBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.TrainingHistoryMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelTrainingByPersonModelUseToolReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorCheckpoint;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorEvent;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorSnapshot;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingMonitorCheckpointRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingMonitorEventRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingMonitorSnapshotRepository;

@Service
@Slf4j
public class TrainingHistoryBOImpl implements TrainingHistoryBO {

    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;

    @Autowired
    ModelUseToolBO modelUseToolBO;

    @Autowired
    TrainingHistoryService trainingHistoryService;

    @Autowired
    TrainingHistoryMapper trainingHistoryMapper;

    @Autowired TrainingMonitorSnapshotRepository snapshotRepository;
    @Autowired TrainingMonitorEventRepository eventRepository;
    @Autowired TrainingMonitorCheckpointRepository checkpointRepository;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public List<TrainingHistoryDTO> getModelTrainingByPersonModelUseTool(GenericTddt4iotsReqDTO<ModelTrainingByPersonModelUseToolReqDTO> request) throws GenericException, IOException, InterruptedException {
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());
        log.warn(personDTO.getId().toString());
        request.getClassDTO().setIdPersonModelUseTool(personDTO.getId());
        List<TrainingHistoryDTO> response = trainingHistoryMapper.trainingHistoryDTOListTo(
                trainingHistoryService.getModelTrainingByPersonModelUseTool(
                        request.getClassDTO().getIdModelPermission(),
                        request.getClassDTO().getIdPersonModelTraining()));

        return response;
    }

    @Override
    public TrainingHistoryDTO validate(String request) throws GenericException, IOException, InterruptedException {
        ModelUseToolDto modelUseToolDto = modelUseToolBO.validate(request);
        return trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.findId(modelUseToolDto.getModelTraining().getId()));
    }

    @Override
    public List<TrainingHistoryDTO> getAllTraniningHistory(String userToken) throws GenericException, IOException, InterruptedException {
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(userToken);
        List<TrainingHistoryDTO> trainingHistoryDTOList = trainingHistoryMapper.trainingHistoryDTOListTo(trainingHistoryService.findIdByPerson(personDTO.getId()));

        if(trainingHistoryDTOList.isEmpty()) {
            throw new GenericException("There is no data.");
        }

        return trainingHistoryDTOList;
    }

    @Override
    public JsonNode getMonitorDetail(Long trainingHistoryId, String userToken)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = tddt4iotsGenericBO.validateSession(userToken);
        TrainingHistory history = trainingHistoryService.findId(trainingHistoryId);
        if (history.getIdPerson() == null || !person.getId().equals(history.getIdPerson().getId())) {
            throw new GenericException("The training does not belong to the authenticated user.");
        }
        ObjectNode result = JSON.createObjectNode();
        result.put("trainingHistoryId", history.getId());
        result.put("providerJobId", history.getProviderJobId());
        result.put("providerStatus", history.getProviderStatus());
        if (history.getProgressPercent() == null) result.putNull("progressPercent"); else result.put("progressPercent", history.getProgressPercent());
        if (history.getLastMonitoredAt() == null) result.putNull("lastMonitoredAt"); else result.put("lastMonitoredAt", history.getLastMonitoredAt().getTime());
        result.put("monitoringFailures", history.getMonitoringFailures() == null ? 0 : history.getMonitoringFailures());

        ArrayNode snapshots = result.putArray("snapshots");
        for (TrainingMonitorSnapshot item : snapshotRepository.findTop100ByTrainingHistoryIdOrderByCheckedAtDesc(trainingHistoryId)) {
            ObjectNode node = snapshots.addObject();
            node.put("checkedAt", item.getCheckedAt() == null ? null : item.getCheckedAt().getTime());
            node.put("status", item.getProviderStatus());
            if (item.getProgressPercent() == null) node.putNull("progressPercent"); else node.put("progressPercent", item.getProgressPercent());
            node.put("durationMs", item.getDurationMs());
            node.put("error", item.getErrorMessage());
        }
        ArrayNode events = result.putArray("events");
        for (TrainingMonitorEvent item : eventRepository.findByTrainingHistoryIdOrderByProviderCreatedAtDesc(trainingHistoryId)) {
            ObjectNode node = events.addObject();
            node.put("id", item.getProviderEventId());
            node.put("level", item.getLevel());
            node.put("message", item.getMessage());
            node.put("createdAt", item.getProviderCreatedAt());
            putJson(node, "data", item.getDataPayload());
        }
        ArrayNode checkpoints = result.putArray("checkpoints");
        for (TrainingMonitorCheckpoint item : checkpointRepository.findByTrainingHistoryIdOrderByStepNumberAsc(trainingHistoryId)) {
            ObjectNode node = checkpoints.addObject();
            node.put("id", item.getProviderCheckpointId());
            node.put("stepNumber", item.getStepNumber());
            node.put("model", item.getFineTunedModel());
            node.put("createdAt", item.getProviderCreatedAt());
            putJson(node, "metrics", item.getMetricsPayload());
        }
        return result;
    }

    private void putJson(ObjectNode target, String field, String raw) {
        try { target.set(field, raw == null ? JSON.nullNode() : JSON.readTree(raw)); }
        catch (Exception ignored) { target.put(field, raw); }
    }
}
