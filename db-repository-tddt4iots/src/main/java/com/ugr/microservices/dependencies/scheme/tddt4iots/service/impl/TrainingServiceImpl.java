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
    public TrainingHistory findId(Long id) throws GenericException {
        Optional<TrainingHistory> trainingHistory = trainingHistoryRepository.findById(id.intValue());

        if(!trainingHistory.isPresent()) {
            throw new GenericException("There is no record with the filtered ID.");
        }

        return trainingHistory.get();
    }

    @Override
    public TrainingHistory findIdByModelPermission(Long id) throws GenericException {
        Optional<TrainingHistory> trainingHistory = trainingHistoryRepository.findIdByModelPermission(id);

        if(!trainingHistory.isPresent()) {
            throw new GenericException("There is no record with the filtered ID.");
        }

        return trainingHistory.get();
    }

    @Override
    public List<TrainingHistory> getTrainingHistoriesByIdModel(Long idModel) throws GenericException {
        return trainingHistoryRepository.getTrainingHistoriesByIdModel(idModel);
    }

    @Override
    public TrainingHistory getLastestTraining(Long idModel) throws GenericException {
        Optional<TrainingHistory> trainingHistoryDTO = trainingHistoryRepository.getLastestTraining(idModel);
        if(trainingHistoryDTO.isEmpty()) {
            throw new GenericException("The selected base model does not have prior training.");
        }
        return trainingHistoryDTO.get();
    }

    @Override
    public TrainingHistory getLastestTrainingByNotId(Long idModel, Long idTrainingHistory) throws GenericException {
        Optional<TrainingHistory> trainingHistoryDTO = trainingHistoryRepository.getLastestTrainingByNotId(idModel, idTrainingHistory);
        if(trainingHistoryDTO.isEmpty()) {
            throw new GenericException("The selected base model does not have prior training.");
        }
        return trainingHistoryDTO.get();
    }

    @Override
    public List<TrainingHistory> getModelTrainingByPersonModelUseTool(Long idModelPermission, Long idPersonModelTraining) throws GenericException {
        List<TrainingHistory> response = trainingHistoryRepository.getModelTrainingByPersonModelUseTool(idModelPermission, idPersonModelTraining);
        if(response.isEmpty()) {
            throw new GenericException("The base model trained does not have prior training to be used in the tool.");
        }

        return response;
    }

    @Override
    public void deleteTraining(TrainingHistory request) throws GenericException {
        trainingHistoryRepository.delete(request);
    }

    @Override
    public List<TrainingHistory> findIdByPerson(Long idPerson) throws GenericException {
        return trainingHistoryRepository.findIdByPerson(idPerson);
    }


}
