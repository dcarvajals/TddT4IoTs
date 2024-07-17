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
}
