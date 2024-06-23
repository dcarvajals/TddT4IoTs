package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.MasterProjectBO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.MasterProjectDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.CreateFileTrainingReq;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GetProjectFromDateReq;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.TrainingModelOpenAiReq;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.CreateFileTrainingResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.TrainingModelOpenAiRes;
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
        path = "/master-project"
)
public class MasterProjectController {

    @Autowired
    private MasterProjectBO masterProjectBO;

    @GetMapping(
            path = "/list",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<MasterProjectDTO>> list () throws GenericException {
        GenericBasicResponse<List<MasterProjectDTO>> response = new GenericBasicResponse<>();
        log.info("Request received: list");
        response.setData(masterProjectBO.getAllProjectsActive());
        log.info("Request completed: list");
        return response;
    }

    @GetMapping(
            path = "/listProjectFromToDate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<MasterProjectDTO>> listProjectFromToDate (
            @RequestBody GetProjectFromDateReq getProjectFromDateReq
            ) throws GenericException {
        GenericBasicResponse<List<MasterProjectDTO>> response = new GenericBasicResponse<>();
        log.info("Request received: listProjectFromToDate");
        response.setData(masterProjectBO.getProjectFromToDate(getProjectFromDateReq));
        log.info("Request completed: listProjectFromToDate");
        return response;
    }

    @GetMapping(
            path = "/createFileTraining",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<CreateFileTrainingResDTO> createFileTraining (
            @RequestBody CreateFileTrainingReq createFileTrainingReq,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<CreateFileTrainingResDTO> response = new GenericBasicResponse<>();
        log.info("Request received: listProjectFromToDate");
        createFileTrainingReq.setUserToken(userToken);
        response.setData(masterProjectBO.createFileTraining(createFileTrainingReq));
        log.info("Request completed: listProjectFromToDate");
        return response;
    }

    @GetMapping(
            path = "/trainingModelOpenAi",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<TrainingModelOpenAiRes>> trainingModelOpenAi (
            @RequestBody TrainingModelOpenAiReq trainingModelOpenAiReq
    ) throws GenericException, IOException {
        GenericBasicResponse<List<TrainingModelOpenAiRes>> response = new GenericBasicResponse<>();
        log.info("Request received: listProjectFromToDate");
        response.setData(masterProjectBO.trainingModelOpenAi(trainingModelOpenAiReq));
        log.info("Request completed: listProjectFromToDate");
        return response;
    }


}
