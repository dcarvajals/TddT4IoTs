package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface PersonBO {
    PersonDTO saveSecretKeyOpenAi (GenericTddt4iotsReqDTO<PersonDTO> request) throws GenericException, IOException, InterruptedException;
}
