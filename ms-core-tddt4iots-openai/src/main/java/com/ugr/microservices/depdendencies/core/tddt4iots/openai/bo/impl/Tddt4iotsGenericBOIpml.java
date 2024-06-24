package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.config.ApplicationConfig;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.PersonMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ValidateTokenReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.ValidateTokenResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class Tddt4iotsGenericBOIpml implements Tddt4iotsGenericBO {
    private final ApplicationConfig applicationConfig;

    @Autowired
    PersonMapper personMapper;

    @Autowired
    PersonService personService;

    public Tddt4iotsGenericBOIpml(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public PersonDTO validateSession(String userToken) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        ValidateTokenReqDTO validateTokenReqDTO = ValidateTokenReqDTO.builder()
                .user_token(userToken)
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

        return  personMapper.personDTOTo(
                personService.findById(validateTokenResDTO.getData().getUserId())
        );
    }
}
