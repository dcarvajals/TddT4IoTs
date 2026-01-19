package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface Tddt4iotsGenericBO {
    PersonDTO validateSession (String userToken) throws GenericException, IOException, InterruptedException;
}
