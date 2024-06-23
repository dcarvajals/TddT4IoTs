package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelServiceImpl implements ModelService {
    @Autowired
    ModelRepository modelRepository;

    @Override
    public List<Model> getAllModel() throws GenericException {
        return modelRepository.getAllModel();
    }

    @Override
    public Model save(Model request) throws GenericException {
        return modelRepository.save(request);
    }
}
