package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingMonitorCheckpointRepository extends JpaRepository<TrainingMonitorCheckpoint, Long> {
    boolean existsByProviderCheckpointId(String providerCheckpointId);
    List<TrainingMonitorCheckpoint> findByTrainingHistoryIdOrderByStepNumberAsc(Long trainingHistoryId);
}
