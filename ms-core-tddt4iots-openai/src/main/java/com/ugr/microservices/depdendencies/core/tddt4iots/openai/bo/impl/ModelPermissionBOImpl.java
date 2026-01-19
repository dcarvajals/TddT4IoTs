package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelPermissionBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelUseToolBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelPermissionMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelPermissionFromModelReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

        // validar si ya tiene un registro de permiso, caso contrario solo se edita el actual
        List<ModelPermissionDTO> modelPermissionDTOList =  modelPermissionMapper.modelPermissionDTOListTo(
                modelPermissionService.getAllModelPermission(personDTO.getId()));

        if(!modelPermissionDTOList.isEmpty()){
            ModelPermissionDTO modelPermissionDTO = modelPermissionDTOList.getFirst();
            request.getClassDTO().setId(modelPermissionDTO.getId());
        }

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

        ModelUseToolDto modelUseToolDto = ModelUseToolDto.builder()
                .model(request.getClassDTO().getModel())
                .person(personDTO)
                .build();
        modelUseToolBO.save(modelUseToolDto);

        // validar si ya tiene un registro de permiso, caso contrario solo se edita el actual
        List<ModelPermissionDTO> modelPermissionDTOList =  modelPermissionMapper.modelPermissionDTOListTo(
                modelPermissionService.getAllModelPermission(personDTO.getId()));

        if(!modelPermissionDTOList.isEmpty()){
            ModelPermissionDTO modelPermissionDTO = modelPermissionDTOList.getFirst();
            request.getClassDTO().setId(modelPermissionDTO.getId());
        }

        return modelPermissionMapper.modelPermissionDTOTo(
                modelPermissionService.save(modelPermissionMapper.modelPermissionTo(request.getClassDTO())));
    }

    @Override
    public ModelPermissionDTO validatePermiss(String request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request);
        List<ModelPermissionDTO> modelPermissionDTOList =  modelPermissionMapper.modelPermissionDTOListTo(
                modelPermissionService.getAllModelPermission(personDTO.getId()));

        if(modelPermissionDTOList.isEmpty()) {
            throw new GenericException ("The user does not have permissions to access the OpenAI functionality.", 3);
        }

        return modelPermissionDTOList.getFirst();
    }

    @Override
    public List<ModelPermissionDTO> getModelPermissionFromModel(GenericTddt4iotsReqDTO<ModelPermissionFromModelReqDTO> request) throws GenericException, IOException, InterruptedException {
        tddt4iotsGenericBO.validateSession(request.getUserToken());

        List<ModelPermissionDTO> response = modelPermissionMapper.modelPermissionDTOListTo(
                modelPermissionService.getModelPermissionFromModel(request.getClassDTO().getIdModel()));

        return response;
    }


}
