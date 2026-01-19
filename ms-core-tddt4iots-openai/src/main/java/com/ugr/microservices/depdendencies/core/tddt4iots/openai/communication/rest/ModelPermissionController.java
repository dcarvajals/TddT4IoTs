package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelPermissionBO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelPermissionFromModelReqDTO;
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
        path = "/model-permission"
)
public class ModelPermissionController {
    @Autowired
    ModelPermissionBO modelPermissionBO;

    @PostMapping(
            path = "/create-train",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelPermissionDTO> createTrain (
            @RequestBody GenericTddt4iotsReqDTO<ModelPermissionDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelPermissionDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: createTrain");
        response.setData(modelPermissionBO.createTrain(genericTddt4iotsReqDTO));
        log.info("Request completed: createTrain");
        return response;
    }

    @PostMapping(
            path = "/create-use",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelPermissionDTO> createUse (
            @RequestBody GenericTddt4iotsReqDTO<ModelPermissionDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelPermissionDTO> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: createUse");
        response.setData(modelPermissionBO.createUse(genericTddt4iotsReqDTO));
        log.info("Request completed: createUse");
        return response;
    }

    @GetMapping(
            path = "/validate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelPermissionDTO> validate (
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelPermissionDTO> response = new GenericBasicResponse<>();
        log.info("Request received: validate");
        response.setData(modelPermissionBO.validatePermiss(userToken));
        log.info("Request completed: validate");
        return response;
    }

    @PostMapping(
            path = "/permission-models",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<ModelPermissionDTO>> permissionModels (
            @RequestBody GenericTddt4iotsReqDTO<ModelPermissionFromModelReqDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<List<ModelPermissionDTO>> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: permissionModels");
        response.setData(modelPermissionBO.getModelPermissionFromModel(genericTddt4iotsReqDTO));
        log.info("Request completed: permissionModels");
        return response;
    }

}
