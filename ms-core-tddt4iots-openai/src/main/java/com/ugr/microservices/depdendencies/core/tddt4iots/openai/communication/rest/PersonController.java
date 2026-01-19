package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.PersonBO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.ValidateSecretKeyResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericBasicResponse;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping(
        path = "/person"
)
public class PersonController {

    @Autowired
    PersonBO personBO;

    @PostMapping(
            path = "/create-secret-key-openai",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<PersonDTO> createSecretKeyOpenAi (
            @RequestBody GenericTddt4iotsReqDTO<PersonDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<PersonDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: createSecretKeyOpenAi");
        response.setData(personBO.saveSecretKeyOpenAi(genericTddt4iotsReqDTO));
        log.info("Request completed: createSecretKeyOpenAi");
        return response;
    }

    @GetMapping(
            path = "/validate-secret-key-openai",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ValidateSecretKeyResDTO> validateSecretKeyOpenAi (
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ValidateSecretKeyResDTO> response = new GenericBasicResponse<>();
        log.info("Request received: validateSecretKeyOpenAi");
        response.setData(personBO.validateSecretKey(userToken));
        log.info("Request completed: validateSecretKeyOpenAi");
        return response;
    }

}
