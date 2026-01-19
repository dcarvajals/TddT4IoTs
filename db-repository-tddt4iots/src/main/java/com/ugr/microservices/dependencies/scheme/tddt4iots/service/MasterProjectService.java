package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GetProjectFromDateReq;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.MasterProject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MasterProjectService {
    List<MasterProject> getAllProjectsActive () throws GenericException;
    List<MasterProject> getProjectFromToDate (GetProjectFromDateReq request) throws GenericException;
}
