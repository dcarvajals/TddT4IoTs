package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenericTddt4iotsReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.ModelPermissionFromModelReqDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ModelPermissionBO {
    ModelPermissionDTO createTrain (GenericTddt4iotsReqDTO<ModelPermissionDTO> request) throws GenericException, IOException, InterruptedException;
    ModelPermissionDTO createUse (GenericTddt4iotsReqDTO<ModelPermissionDTO> request) throws GenericException, IOException, InterruptedException;
    ModelPermissionDTO validatePermiss (String request) throws GenericException, IOException, InterruptedException;
    List<ModelPermissionDTO> getModelPermissionFromModel (GenericTddt4iotsReqDTO<ModelPermissionFromModelReqDTO> request) throws GenericException, IOException, InterruptedException;
}