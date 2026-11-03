package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingMonitorEventRepository extends JpaRepository<TrainingMonitorEvent, Long> {
    boolean existsByProviderEventId(String providerEventId);
    List<TrainingMonitorEvent> findByTrainingHistoryIdOrderByProviderCreatedAtDesc(Long trainingHistoryId);
}
