package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelUseToolBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl.SharedModelCatalogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericBasicResponse;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelUseToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping(
        path = "/model-use-tool"
)
public class ModelUseToolController {

    @Autowired
    ModelUseToolBO modelUseToolBO;

    @Autowired
    SharedModelCatalogService catalog;

    @Autowired
    Tddt4iotsGenericBO sessions;

    @GetMapping(path = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<java.util.List<SharedModelCatalogService.Entry>> catalog(@RequestHeader String userToken)
            throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<java.util.List<SharedModelCatalogService.Entry>> response = new GenericBasicResponse<>();
        response.setData(catalog.list(sessions.validateSession(userToken).getId()));
        return response;
    }

    @PostMapping(path = "/select", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<SharedModelCatalogService.Entry> select(@RequestHeader String userToken, @RequestBody JsonNode request)
            throws GenericException, IOException, InterruptedException {
        Long userId = sessions.validateSession(userToken).getId();
        if (!request.path("registrationId").canConvertToLong()) throw new GenericException("registrationId es obligatorio.");
        GenericBasicResponse<SharedModelCatalogService.Entry> response = new GenericBasicResponse<>();
        response.setData(catalog.select(userId, request.path("registrationId").asLong(), request.path("registrationKind").asText("TRAINED_MODEL")));
        return response;
    }

    @GetMapping(
            path = "/validate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<ModelUseToolDto> validate (
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<ModelUseToolDto> response = new GenericBasicResponse<>();
        log.info("Request received: validate");
        response.setData(modelUseToolBO.validate(userToken));
        log.info("Request completed: validate");
        return response;
    }

}
