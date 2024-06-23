package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import org.springframework.stereotype.Service;

@Service
public interface PersonService {
    Person findById(Long id) throws GenericException;
}
