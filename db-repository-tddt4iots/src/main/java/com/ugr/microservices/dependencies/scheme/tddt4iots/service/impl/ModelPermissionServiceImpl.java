package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
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
}
