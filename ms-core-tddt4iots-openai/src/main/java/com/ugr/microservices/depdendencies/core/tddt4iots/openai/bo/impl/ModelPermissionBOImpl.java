package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelPermissionBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelUseToolBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelPermissionMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ModelPermissionBOImpl implements ModelPermissionBO {
    @Autowired
    ModelPermissionService modelPermissionService;

    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;

    @Autowired
    ModelUseToolBO modelUseToolBO;

    @Autowired
    ModelPermissionMapper modelPermissionMapper;

    @Override
    public ModelPermissionDTO createTrain(GenericTddt4iotsReqDTO<ModelPermissionDTO> request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());
        request.getClassDTO().setActive(Boolean.TRUE);
        request.getClassDTO().setPerson(personDTO);
        request.getClassDTO().setPrimaryTrain(Boolean.TRUE);
        request.getClassDTO().setCanUse(Boolean.FALSE);
        request.getClassDTO().setCanTrain(Boolean.TRUE);
        return modelPermissionMapper.modelPermissionDTOTo(
                modelPermissionService.save(modelPermissionMapper.modelPermissionTo(request.getClassDTO())));
    }

    @Override
    public ModelPermissionDTO createUse(GenericTddt4iotsReqDTO<ModelPermissionDTO> request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());
        request.getClassDTO().setActive(Boolean.TRUE);
        request.getClassDTO().setPerson(personDTO);
        request.getClassDTO().setPrimaryTrain(Boolean.TRUE);
        request.getClassDTO().setCanUse(Boolean.TRUE);
        request.getClassDTO().setCanTrain(Boolean.FALSE);

        // crear el nuevo registro para uso del modelo dentro de la herramienta
        ModelUseToolDto modelUseToolDto = ModelUseToolDto.builder()
                .model(request.getClassDTO().getModel())
                .person(personDTO)
                .build();

        modelUseToolBO.save(modelUseToolDto);

        return modelPermissionMapper.modelPermissionDTOTo(
                modelPermissionService.save(modelPermissionMapper.modelPermissionTo(request.getClassDTO())));
    }
}
