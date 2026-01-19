package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelPermission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ModelPermissionService {
    List<ModelPermission> getAllModelPermission (Long idPerson) throws GenericException;
    ModelPermission save (ModelPermission request) throws GenericException;
    List<ModelPermission> getAllModelPermissionFromModel (Long idModel) throws GenericException;
    List<ModelPermission> getModelPermissionFromModel (Long idModel) throws GenericException;
}
