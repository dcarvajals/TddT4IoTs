package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.TrainingHistoryBO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelTrainingByPersonModelUseToolReqDTO;
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
        path = "/training-history"
)
public class TrainingHistoryController {

    @Autowired
    TrainingHistoryBO trainingHistoryBO;

    @PostMapping(
            path = "/training-from-base-models",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<TrainingHistoryDTO>> trainingFromBaseModels (
            @RequestBody GenericTddt4iotsReqDTO<ModelTrainingByPersonModelUseToolReqDTO> genericTddt4iotsReqDTO,
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<List<TrainingHistoryDTO>> response = new GenericBasicResponse<>();
        genericTddt4iotsReqDTO.setUserToken(userToken);
        log.info("Request received: trainingFromBaseModels");
        response.setData(trainingHistoryBO.getModelTrainingByPersonModelUseTool(genericTddt4iotsReqDTO));
        log.info("Request completed: trainingFromBaseModels");
        return response;
    }

    @GetMapping(
            path = "/validate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<TrainingHistoryDTO> validate (
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<TrainingHistoryDTO> response = new GenericBasicResponse<>();
        log.info("Request received: validate");
        response.setData(trainingHistoryBO.validate(userToken));
        log.info("Request completed: validate");
        return response;
    }

    @GetMapping(
            path = "/list",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GenericBasicResponse<List<TrainingHistoryDTO>> list (
            @RequestHeader String userToken
    ) throws GenericException, IOException, InterruptedException {
        GenericBasicResponse<List<TrainingHistoryDTO>> response = new GenericBasicResponse<>();
        log.info("Request received: list");
        response.setData(trainingHistoryBO.getAllTraniningHistory(userToken));
        log.info("Request completed: list");
        return response;
    }
}
