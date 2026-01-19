package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.MasterProjectDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.CreateFileTrainingResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.TrainingModelOpenAiRes;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface MasterProjectBO {
    List<MasterProjectDTO> getAllProjectsActive () throws GenericException;
    List<MasterProjectDTO> getProjectFromToDate (GetProjectFromDateReq request) throws GenericException;
    CreateFileTrainingResDTO createFileTraining (CreateFileTrainingReq request) throws GenericException, IOException, InterruptedException;
    List<TrainingModelOpenAiRes> trainingModelOpenAi (TrainingModelOpenAiReq request) throws GenericException;
    String useModelOpenAi (GenericTddt4iotsReqDTO<UseModelOpenaiRedDTO> genericTddt4iotsReqDTO) throws GenericException, IOException, InterruptedException;
    String deleteTrainingHistory (GenericTddt4iotsReqDTO<Long> idTrainingHistory) throws GenericException;
}
