package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;


import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.PersonRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonImplService implements PersonService {
    @Autowired
    PersonRepository personRepository;

    @Override
    public Person findById(Long id) throws GenericException {
        Optional<Person> person = personRepository.findById(id);

        if(person.isEmpty()) {
            throw new GenericException ("The person registered in the system does not exist");
        }

        return person.get();
    }

    @Override
    public Person save(Person request) throws GenericException {
        return personRepository.save(request);
    }
}
