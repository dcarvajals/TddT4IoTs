package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ModelOpenAiBO {
    List<ModelDTO> getAllModel () throws GenericException;
    ModelDTO save (GenericTddt4iotsReqDTO<ModelDTO> request) throws GenericException, IOException, InterruptedException;
    ModelDTO update (GenericTddt4iotsReqDTO<ModelDTO> request) throws GenericException, IOException, InterruptedException;
    ModelDTO inactive (ModelDTO request) throws GenericException;
}
