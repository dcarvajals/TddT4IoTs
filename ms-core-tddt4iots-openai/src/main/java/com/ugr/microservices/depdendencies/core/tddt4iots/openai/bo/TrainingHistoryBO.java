package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelTrainingByPersonModelUseToolReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface TrainingHistoryBO {
    List<TrainingHistoryDTO> getModelTrainingByPersonModelUseTool (GenericTddt4iotsReqDTO<ModelTrainingByPersonModelUseToolReqDTO> request) throws GenericException, IOException, InterruptedException;
    TrainingHistoryDTO validate (String request) throws GenericException, IOException, InterruptedException;
    List<TrainingHistoryDTO> getAllTraniningHistory (String userToken) throws GenericException, IOException, InterruptedException;
}
