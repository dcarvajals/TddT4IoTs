package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.PersonBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.PersonMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ValidateTokenReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.ValidateTokenResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PersonBOImpl implements PersonBO {
    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;

    @Autowired
    PersonService personService;

    @Autowired
    PersonMapper personMapper;

    @Override
    public PersonDTO saveSecretKeyOpenAi(GenericTddt4iotsReqDTO<PersonDTO> request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());
        personDTO.setOpenaiSecretKey(request.getClassDTO().getOpenaiSecretKey());
        return personMapper.personDTOTo(personService.save(personMapper.personTo(personDTO)));
    }
}
