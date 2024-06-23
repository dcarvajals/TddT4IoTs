package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelOpenAiBO;
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
import java.util.List;

@Service
@Slf4j
public class ModelOpenAiBOImpl implements ModelOpenAiBO {

    private final ApplicationConfig applicationConfig;
    @Autowired
    ModelService modelService;

    @Autowired
    PersonService personService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PersonMapper personMapper;

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
        ValidateTokenReqDTO validateTokenReqDTO = ValidateTokenReqDTO.builder()
                .user_token(request.getUserToken())
                .build();
        ValidateTokenResDTO validateTokenResDTO = Tddt4iotsUtil.postRequest(
                applicationConfig.getTddt4iotsServer() +
                        Tddt4iotsCons.URL_VALIDATE_TOKEN,
                validateTokenReqDTO,
                ValidateTokenResDTO.class
        );

        if(validateTokenResDTO.getStatus() != 2 && validateTokenResDTO.getData().getUserId() != null) {
            throw new GenericException(validateTokenResDTO.getInformation());
        }

        log.info(validateTokenResDTO.getInformation());
        PersonDTO personDTO = personMapper.personTo(
                personService.findById(validateTokenResDTO.getData().getUserId())
        );

        request.getClassDTO().setIdPerson(personDTO);
        return modelMapper.modelDTOTo(modelService.save(modelMapper.modelTo(request.getClassDTO())));
    }

    @Override
    public ModelDTO inactive(ModelDTO request) throws GenericException {
        request.setActive(Boolean.FALSE);
        return modelMapper.modelDTOTo(modelService.save(modelMapper.modelTo(request)));
    }
}
