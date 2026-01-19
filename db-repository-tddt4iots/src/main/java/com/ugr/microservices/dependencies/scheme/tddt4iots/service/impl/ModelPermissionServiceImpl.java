package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelPermission;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelPermissionRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelPermissionServiceImpl implements ModelPermissionService {
    @Autowired
    ModelPermissionRepository modelPermissionRepository;

    @Override
    public List<ModelPermission> getAllModelPermission(Long idPerson) throws GenericException {
        return modelPermissionRepository.getAllModelPermission(idPerson);
    }

    @Override
    public ModelPermission save(ModelPermission request) throws GenericException {
        return modelPermissionRepository.save(request);
    }

    @Override
    public List<ModelPermission> getAllModelPermissionFromModel(Long idModel) throws GenericException {
        List<ModelPermission> modelPermissions = modelPermissionRepository.getAllModelPermissionFromModel(idModel);
        if(modelPermissions.isEmpty()) {
            throw new GenericException("No user has trained the selected base model, therefore no models are available.");
        }

        return modelPermissionRepository.getAllModelPermissionFromModel(idModel);
    }

    @Override
    public List<ModelPermission> getModelPermissionFromModel(Long idModel) throws GenericException {
        return modelPermissionRepository.getModelPermissionFromModel(idModel);
    }
}
