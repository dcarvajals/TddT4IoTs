package com.ugr.microservices.dependencies.scheme.tddt4iots.service.impl;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GetProjectFromDateReq;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingHistoryRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingHistoryService {

    @Autowired
    TrainingHistoryRepository trainingHistoryRepository;

    @Override
    public TrainingHistory save(TrainingHistory trainingHistory) throws GenericException {
        return trainingHistoryRepository.save(trainingHistory);
    }

    @Override
    public List<TrainingHistory> getTrainingFromToDate(GetProjectFromDateReq getProjectFromDateReq) throws GenericException {
        return trainingHistoryRepository.getTrainingFromToDate(getProjectFromDateReq.getStartDate(), getProjectFromDateReq.getEndDate());
    }

    @Override
    public Optional<TrainingHistory> findId(Long id) throws GenericException {
        return trainingHistoryRepository.findById(id.intValue());
    }

    @Override
    public List<TrainingHistory> getTrainingHistoriesByIdModel(Long idModel) throws GenericException {
        return trainingHistoryRepository.getTrainingHistoriesByIdModel(idModel);
    }

    @Override
    public TrainingHistory getLastestTraining(Long idModel) throws GenericException {
        Optional<TrainingHistory> trainingHistoryDTO = trainingHistoryRepository.getLastestTraining(idModel);
        if(trainingHistoryDTO.isEmpty()) {
            throw new GenericException("The searched record does not exist");
        }
        return trainingHistoryDTO.get();
    }


}
