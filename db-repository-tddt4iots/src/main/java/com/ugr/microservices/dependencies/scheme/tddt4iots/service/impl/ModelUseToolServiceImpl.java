package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelUseTool;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelUseToolRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelUseToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelUseToolServiceImpl implements ModelUseToolService {
    @Autowired
    ModelUseToolRepository modelUseToolRepository;

    @Override
    public List<ModelUseTool> getAllModelUseTool(Long idPerson) throws GenericException {
        return modelUseToolRepository.getAllModelUseTool(idPerson);
    }

    @Override
    public ModelUseTool save(ModelUseTool request) throws GenericException {
        return modelUseToolRepository.save(request);
    }

    @Override
    public ModelUseTool getModelUseToolByPerson(Long idPerson) throws GenericException {
        Optional<ModelUseTool> modelUseTool = modelUseToolRepository.getModelUseToolByPerson(idPerson);

        if(modelUseTool.isEmpty()) {
            throw new GenericException("The searched record does not exist");
        }

        return modelUseTool.get();
    }
}