package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingHistoryRepository extends JpaRepository<TrainingHistory, Integer> {

    @Query(value = "SELECT TH FROM TrainingHistory TH WHERE TH.dateStartTrining = :dateStartTrining AND TH.dateEndTrining = :dateEndTrining")
    List<TrainingHistory> getTrainingFromToDate (
            @Param("dateStartTrining") Date dateStartTrining,
            @Param("dateEndTrining") Date dateEndTrining
    );

    @Query(value = "SELECT TH FROM TrainingHistory TH JOIN TH.idPerson JOIN TH.model " +
            "WHERE TH.model.id = :idModel ORDER BY TH.dateCreation DESC")
    List<TrainingHistory> getTrainingHistoriesByIdModel (
                    @Param("idModel") Long idModel
    );

    @Query(value = "SELECT TH FROM TrainingHistory TH " +
            "WHERE TH.model.model.id = :idModel AND " +
            "TH.dateCreation = (SELECT MAX(TH2.dateCreation) FROM TrainingHistory TH2 WHERE TH2.model.model.id = :idModel)")
    Optional<TrainingHistory> getLastestTraining (
            @Param("idModel") Long idModel
    );

}