package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelOpenAiBO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericBasicResponse;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping(
        path = "/model-open-ai"
)
public class ModelOpenAiController {

    @Autowired
    ModelOpenAiBO modelOpenAiBO;

    @GetMapping(
            path = "/list",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<ModelDTO>> list () throws GenericException {
        GenericBasicResponse<List<ModelDTO>> response = new GenericBasicResponse<>();
        log.info("Request received: list");
        response.setData(modelOpenAiBO.getAllModel());
        log.info("Request completed: list");
        return response;
    }

    @PostMapping(
            path = "/create-model",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelDTO> createModel (
            @RequestBody GenericTddt4iotsReqDTO<ModelDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: createModel");
        response.setData(modelOpenAiBO.save(genericTddt4iotsReqDTO));
        log.info("Request completed: createModel");
        return response;
    }

    @PostMapping(
            path = "/update-model",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelDTO> updateModel (
            @RequestBody GenericTddt4iotsReqDTO<ModelDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: updateModel");
        response.setData(modelOpenAiBO.update(genericTddt4iotsReqDTO));
        log.info("Request completed: updateModel");
        return response;
    }

    @PostMapping(
            path = "/inactive-model",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelDTO> inactiveModel (
            @RequestBody GenericTddt4iotsReqDTO<ModelDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: inactiveModel");
        response.setData(modelOpenAiBO.inactive(genericTddt4iotsReqDTO.getClassDTO()));
        log.info("Request completed: inactiveModel");
        return response;
    }

}
