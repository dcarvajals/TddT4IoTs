package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ModelService {

    List<Model> getAllModel () throws GenericException;

    Model save (Model request) throws GenericException;


}
