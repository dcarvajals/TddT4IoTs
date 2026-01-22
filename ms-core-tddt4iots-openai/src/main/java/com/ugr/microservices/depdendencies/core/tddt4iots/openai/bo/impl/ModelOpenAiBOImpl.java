package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelOpenAiBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.config.ApplicationConfig;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.PersonMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ValidateTokenReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.ValidateTokenResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ModelOpenAiBOImpl implements ModelOpenAiBO {

    private final ApplicationConfig applicationConfig;
    @Autowired
    ModelService modelService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;


    public ModelOpenAiBOImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public List<ModelDTO> getAllModel() throws GenericException {
        return modelMapper.modelListTo(modelService.getAllModel());
    }

    @Override
    public ModelDTO save(GenericTddt4iotsReqDTO<ModelDTO> request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());

        request.getClassDTO().setIdPerson(personDTO);
        request.getClassDTO().setActive(Boolean.TRUE);
        request.getClassDTO().setCreateAt(new Date());
        return modelMapper.modelDTOTo(modelService.save(modelMapper.modelTo(request.getClassDTO())));
    }

    @Override
    public ModelDTO update(GenericTddt4iotsReqDTO<ModelDTO> request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());

        request.getClassDTO().setIdPerson(personDTO);
        return modelMapper.modelDTOTo(modelService.save(modelMapper.modelTo(request.getClassDTO())));
    }

    @Override
    public ModelDTO inactive(ModelDTO request) throws GenericException {
        request.setActive(Boolean.FALSE);
        return modelMapper.modelDTOTo(modelService.save(modelMapper.modelTo(request)));
    }
}
