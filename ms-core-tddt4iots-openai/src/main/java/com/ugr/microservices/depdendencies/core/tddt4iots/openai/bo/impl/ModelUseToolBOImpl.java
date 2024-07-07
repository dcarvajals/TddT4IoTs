package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelUseToolBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelPermissionMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelUseToolMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.TrainingHistoryMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelPermissionService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelUseToolService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ModelUseToolBOImpl implements ModelUseToolBO {
    @Autowired
    ModelUseToolService modelUseToolService;

    @Autowired
    ModelUseToolMapper modelUseToolMapper;

    @Autowired
    TrainingHistoryService trainingHistoryService;

    @Autowired
    TrainingHistoryMapper trainingHistoryMapper;

    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;

    @Autowired
    ModelPermissionService modelPermissionService;

    @Autowired
    ModelPermissionMapper modelPermissionMapper;

    @Override
    public ModelUseToolDto save(ModelUseToolDto modelUseToolDto) throws GenericException, IOException, InterruptedException {

        // buscar primero quienes han tenido permiso de entrenar el modelo seleccionado
        List<ModelPermissionDTO> modelPermissionDTO = modelPermissionMapper.modelPermissionDTOListTo(
                modelPermissionService.getAllModelPermissionFromModel(modelUseToolDto.getModel().getId()));

        // buscar el ultimo modelo de acuerdo al modelo base seleccionado por el usuario
        TrainingHistoryDTO trainingHistoryDTO =  trainingHistoryMapper
                .trainingHistoryDtoTo(trainingHistoryService.getLastestTraining(modelPermissionDTO.getFirst().getModel().getId()));
        modelUseToolDto.setModelTraining(trainingHistoryDTO);
        modelUseToolDto.setModel(trainingHistoryDTO.getModel().getModel());
        modelUseToolDto.setActive(Boolean.TRUE);
        return modelUseToolMapper.modelUseToolDtoTo(
                modelUseToolService.save(modelUseToolMapper.modelUseToolTo(modelUseToolDto)));
    }
}
