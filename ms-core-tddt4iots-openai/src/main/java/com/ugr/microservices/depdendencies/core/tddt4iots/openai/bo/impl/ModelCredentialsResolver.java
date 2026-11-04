package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Resolves credentials from the persisted model registration, never from the caller. */
@Service
@RequiredArgsConstructor
public class ModelCredentialsResolver {
    private final TrainingHistoryService trainingHistoryService;
    private final PersonService personService;
    private final ModelRepository models;

    public String resolve(ModelUseToolDto selection) throws GenericException {
        if (selection == null) {
            throw new GenericException("Seleccione un modelo registrado antes de generar los diagramas.");
        }
        if (selection.getModelTraining() == null) {
            if (selection.getModel() == null || selection.getModel().getId() == null)
                throw new GenericException("Seleccione un modelo registrado antes de generar los diagramas.");
            Model model = models.findById(selection.getModel().getId()).orElse(null);
            if (model == null || !Boolean.TRUE.equals(model.getActive()) || model.getIdPerson() == null)
                throw new GenericException("El modelo registrado no está disponible.");
            return ownerKey(model.getIdPerson().getId());
        }
        if (selection.getModelTraining().getId() == null)
            throw new GenericException("Seleccione una versión registrada del modelo.");
        TrainingHistory registration = trainingHistoryService.findId(selection.getModelTraining().getId());
        if (registration == null || registration.getIdPerson() == null
                || registration.getIdPerson().getId() == null) {
            throw new GenericException("El modelo seleccionado no tiene un propietario registrado.");
        }
        return ownerKey(registration.getIdPerson().getId());
    }

    private String ownerKey(Long ownerId) throws GenericException {
        Person owner = personService.findById(ownerId);
        String key = owner == null ? null : owner.getOpenaiSecretKey();
        if (key == null || key.isBlank()) {
            throw new GenericException("El propietario del modelo seleccionado debe configurar su clave de OpenAI.");
        }
        return key;
    }
}
