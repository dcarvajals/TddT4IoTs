package com.ugr.microservices.dependencies.scheme.tddt4iots.service;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GetProjectFromDateReq;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface TrainingHistoryService {
    TrainingHistory save (TrainingHistory trainingHistory) throws GenericException;
    List<TrainingHistory> getTrainingFromToDate (GetProjectFromDateReq getProjectFromDateReq) throws  GenericException;
    Optional<TrainingHistory> findId (Long id) throws GenericException;
    List<TrainingHistory> getTrainingHistoriesByIdModel (Long idModel) throws GenericException;
}
