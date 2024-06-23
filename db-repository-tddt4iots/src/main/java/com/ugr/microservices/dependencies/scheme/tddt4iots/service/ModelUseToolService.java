package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelUseTool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ModelUseToolService {
    List<ModelUseTool> getAllModelUseTool (Long idPerson) throws GenericException;
    ModelUseTool save (ModelUseTool request) throws GenericException;
}
