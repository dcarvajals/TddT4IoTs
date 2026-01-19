package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GetProjectFromDateReq;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.MasterProject;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.MasterProjectRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.MasterProjectService;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterProjectServiceImpl implements MasterProjectService {

    @Autowired
    MasterProjectRepository masterProjectRepository;

    @Override
    public List<MasterProject> getAllProjectsActive() throws GenericException {
        return masterProjectRepository.getAllProjectsActive();
    }

    @Override
    public List<MasterProject> getProjectFromToDate(GetProjectFromDateReq request) throws GenericException {
        return masterProjectRepository.getProjectFromToDate(
                request.getStartDate(), request.getEndDate()
        );
    }

}
